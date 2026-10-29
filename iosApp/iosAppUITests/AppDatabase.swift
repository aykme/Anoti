import Foundation
import SQLite3

/// The app's Room database file, edited by the screenshot test through the simulator's shared file
/// system. The app must not run during an edit: Room holds the file open and notices only its own
/// writes, so the app sees an edit after its next launch.
struct AppDatabase {
    /// The database file's full path.
    let path: String

    /// Finds the database of the app with `bundleId` among the data containers beside the test
    /// runner's own. The newest container wins, since a reinstall leaves a new one.
    static func locate(bundleId: String) throws -> AppDatabase {
        let containers = URL(fileURLWithPath: NSHomeDirectory()).deletingLastPathComponent()
        let folders = try FileManager.default.contentsOfDirectory(
            at: containers,
            includingPropertiesForKeys: [.contentModificationDateKey]
        )
        let newest = folders
            .filter { owner(of: $0) == bundleId }
            .max { modified($0) < modified($1) }
        guard let newest else {
            throw AppDatabaseError.notFound(
                "no data container of \(bundleId) among \(folders.count) in \(containers.path)"
            )
        }
        return AppDatabase(path: newest.appendingPathComponent("Documents/\(table)").path)
    }

    /// Applies `edit` in one transaction. Every edit also turns extra info off on every favorite,
    /// since that mode survives a relaunch.
    func apply(_ edit: DatabaseEdit) throws {
        let named = (edit.newEpisodes ?? []) + Array(edit.episodesViewed.keys)
        try requireRows(named)
        var statements = ["UPDATE \(table) SET is_extra_info_enabled = 0"]
        statements += edit.unfollow.map { "DELETE FROM \(table) WHERE name = \(Self.literal($0))" }
        if let newEpisodes = edit.newEpisodes {
            statements.append(
                "UPDATE \(table) SET is_new_episode = (name IN (\(Self.list(newEpisodes))))"
            )
        }
        statements += edit.episodesViewed.map { title, count in
            "UPDATE \(table) SET episodes_viewed = \(count) WHERE name = \(Self.literal(title))"
        }
        try run(statements)
    }

    /// Readies one update pass that notifies exactly `titles`: lowers their aired count below any
    /// real one and clears every flag and extra-info mode. An announced title is refused, since
    /// it would be notified as episode 0.
    func lowerAiredEpisodes(of titles: [String]) throws {
        try requireRows(titles)
        let announced = try strings(
            "SELECT name FROM \(table) WHERE release_status = 'ANNOUNCED' "
                + "AND name IN (\(Self.list(titles)))"
        )
        guard announced.isEmpty else {
            throw AppDatabaseError.refused("announced titles cannot be notified: \(announced)")
        }
        try run([
            "UPDATE \(table) SET is_new_episode = 0, is_extra_info_enabled = 0",
            "UPDATE \(table) SET episodes_aired = \(loweredAiredCount) "
                + "WHERE name IN (\(Self.list(titles)))"
        ])
    }

    /// Whether every one of `titles` still carries the lowered aired count, so no pass has run.
    func stillLowered(_ titles: [String]) throws -> Bool {
        let lowered = try strings(
            "SELECT name FROM \(table) WHERE episodes_aired = \(loweredAiredCount) "
                + "AND name IN (\(Self.list(titles)))"
        )
        return Set(lowered) == Set(titles)
    }

    /// The names of every favorite.
    func names() throws -> [String] {
        try strings("SELECT name FROM \(table)")
    }

    /// The names of the favorites flagged with a new episode.
    func newEpisodeNames() throws -> [String] {
        try strings("SELECT name FROM \(table) WHERE is_new_episode = 1")
    }

    /// The names of the favorites showing extra info, a mode the app saves with each favorite.
    func extraInfoNames() throws -> [String] {
        try strings("SELECT name FROM \(table) WHERE is_extra_info_enabled = 1")
    }

    // A name in a statement is a literal, its quotes doubled. Names come from the scenario.
    private static func literal(_ text: String) -> String {
        "'" + text.replacingOccurrences(of: "'", with: "''") + "'"
    }

    private static func list(_ texts: [String]) -> String {
        texts.map(literal).joined(separator: ", ")
    }

    private static func owner(of container: URL) -> String? {
        let metadata = container.appendingPathComponent(metadataFile)
        guard
            let data = try? Data(contentsOf: metadata),
            let plist = try? PropertyListSerialization.propertyList(from: data, format: nil),
            let values = plist as? [String: Any]
        else { return nil }
        return values[ownerKey] as? String
    }

    private static func modified(_ url: URL) -> Date {
        let values = try? url.resourceValues(forKeys: [.contentModificationDateKey])
        return values?.contentModificationDate ?? .distantPast
    }

    private func requireRows(_ titles: [String]) throws {
        let present = Set(try names())
        let missing = titles.filter { !present.contains($0) }
        guard missing.isEmpty else {
            throw AppDatabaseError.notFound("not among the favorites: \(missing)")
        }
    }

    private func run(_ statements: [String]) throws {
        try withHandle(flags: SQLITE_OPEN_READWRITE) { handle in
            try execute("BEGIN IMMEDIATE", on: handle)
            do {
                for statement in statements {
                    try execute(statement, on: handle)
                }
                try execute("COMMIT", on: handle)
            } catch {
                try? execute("ROLLBACK", on: handle)
                throw error
            }
        }
    }

    // Read-only, so a read while the app runs leaves its file alone.
    private func strings(_ query: String) throws -> [String] {
        try withHandle(flags: SQLITE_OPEN_READONLY) { handle in
            var statement: OpaquePointer?
            guard
                sqlite3_prepare_v2(handle, query, wholeString, &statement, nil) == SQLITE_OK,
                let prepared = statement
            else {
                throw AppDatabaseError.sqlite("\(message(of: handle)) in: \(query)")
            }
            defer { sqlite3_finalize(prepared) }
            var rows: [String] = []
            while sqlite3_step(prepared) == SQLITE_ROW {
                if let text = sqlite3_column_text(prepared, 0) {
                    rows.append(String(cString: text))
                }
            }
            return rows
        }
    }

    private func execute(_ sql: String, on handle: OpaquePointer) throws {
        guard sqlite3_exec(handle, sql, nil, nil, nil) == SQLITE_OK else {
            throw AppDatabaseError.sqlite("\(message(of: handle)) in: \(sql)")
        }
    }

    // Closed before the body's result is returned, so the app's next launch finds the file free.
    private func withHandle<Result>(
        flags: Int32,
        _ body: (OpaquePointer) throws -> Result
    ) throws -> Result {
        var handle: OpaquePointer?
        guard sqlite3_open_v2(path, &handle, flags, nil) == SQLITE_OK,
              let opened = handle
        else {
            let reason = handle.map(message) ?? "no handle"
            sqlite3_close_v2(handle)
            throw AppDatabaseError.sqlite("cannot open \(path): \(reason)")
        }
        defer { sqlite3_close_v2(opened) }
        return try body(opened)
    }

    private func message(of handle: OpaquePointer) -> String {
        String(cString: sqlite3_errmsg(handle))
    }
}

/// Why the screenshot test could not read or edit the app's database.
enum AppDatabaseError: Error {
    /// No container, or no favorite, by the name given.
    case notFound(String)

    /// The scenario asked for an edit the app would show wrongly.
    case refused(String)

    /// SQLite failed; the message and the statement are attached.
    case sqlite(String)
}

/// The table Room keeps the favorites in, named after the database file.
private let table = "anoti_anime_table"

/// The file a simulator writes into each data container, naming the app that owns it.
private let metadataFile = ".com.apple.mobile_container_manager.metadata.plist"

/// The metadata key that holds the owner's bundle id.
private let ownerKey = "MCMMetadataIdentifier"

/// An aired count below any real one, so the next pass sees a new episode.
private let loweredAiredCount = -1

/// Tells `sqlite3_prepare_v2` to read the query up to its terminating zero.
private let wholeString: Int32 = -1
