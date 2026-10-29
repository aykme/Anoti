import XCTest

/// Makes the app's screenshots for its App Store page. App Store Connect wants a set for each
/// device size, and the project has no Mac to take them on. So this test composes each screen of
/// the scenario below in the real app, on CI's simulators, against the live backend. It keeps
/// every screen as an attachment.
///
/// Only the `store-screenshots` job of `ios.yml` runs it, started with `store_screenshots`. Any
/// other run skips it. The job's `ios-store-screenshots-<slug>` artifact holds the finished shots,
/// ready for upload. Unlike the other UI tests, it asserts the live titles and positions it
/// composes, since composing them is its purpose.
///
/// A new version edits the scenario: the titles and the steps of each device.
final class StoreScreenshotsTests: XCTestCase {
    private let environment = ProcessInfo.processInfo.environment

    override func setUp() {
        continueAfterFailure = false
    }

    @MainActor
    func testIPhoneScreenshots() throws {
        //Given
        try XCTSkipUnless(environment["ANOTI_STORE_SCREENSHOTS"] == "1", Self.onlyTheJob)
        try XCTSkipUnless(UIDevice.current.userInterfaceIdiom == .phone, "an iPhone's shots")
        let driver = ScreenshotDriver(test: self, titles: Self.titles)
        try driver.launch(expectedSize: CGSize(width: 1_206, height: 2_622))

        //When
        try driver.run(Self.iPhoneSteps)

        //Then
        XCTAssertEqual(driver.keptShots, Self.shots(in: Self.iPhoneSteps))
    }

    @MainActor
    func testIPadScreenshots() throws {
        //Given
        try XCTSkipUnless(environment["ANOTI_STORE_SCREENSHOTS"] == "1", Self.onlyTheJob)
        try XCTSkipUnless(UIDevice.current.userInterfaceIdiom == .pad, "an iPad's shots")
        defer { XCUIDevice.shared.orientation = .portrait }
        let driver = ScreenshotDriver(test: self, titles: Self.titles)
        try driver.launch(expectedSize: CGSize(width: 2_064, height: 2_752))

        //When
        try driver.run(Self.iPadSteps)

        //Then
        XCTAssertEqual(driver.keptShots, Self.shots(in: Self.iPadSteps))
    }

    private static let onlyTheJob = "made by the store-screenshots job of ios.yml"

    private static func shots(in steps: [ScreenshotStep]) -> [String] {
        steps.compactMap { step in
            if case let .shoot(name) = step {
                return name
            }
            return nil
        }
    }

    // MARK: - Scenario

    private static let steelBallRun = "Steel Ball Run: JoJo no Kimyou na Bouken"
    private static let blackClover = "Black Clover 2nd Season"
    private static let onePiece = "One Piece"
    private static let kusuriya = "Kusuriya no Hitorigoto 3rd Season"
    private static let sekaiSaikou =
        "Sekai Saikou no Ansatsusha, Isekai Kizoku ni Tensei suru Season 2"
    private static let mashle = "Mashle: Sanma Taisou Shinkakusha Saishuu Shiken-hen"
    private static let bocchi = "Bocchi the Rock! 2nd Season"
    private static let frierenGold = "Sousou no Frieren: Ougonkyou-hen"
    private static let frieren2 = "Sousou no Frieren 2nd Season"
    private static let dandadan = "Dandadan 3rd Season"
    private static let edgerunners = "Cyberpunk: Edgerunners"
    private static let edgerunners2 = "Cyberpunk: Edgerunners 2"
    private static let maou = "Maou 2099"

    private static let titles = [
        steelBallRun, blackClover, onePiece, kusuriya, sekaiSaikou, mashle, bocchi, frierenGold,
        frieren2, dandadan, edgerunners, edgerunners2, maou
    ]

    /// iPhone 17 Pro, upright throughout.
    private static let iPhoneSteps: [ScreenshotStep] = [
        .follow([steelBallRun, mashle, edgerunners2]),
        .onAir(dateMode: [blackClover]),
        .shoot("iphone-01"),
        .soon(top: Anchor(title: sekaiSaikou, raisedBy: 44), dateMode: [mashle]),
        .shoot("iphone-02"),
        .search("cyberpunk", shows: [edgerunners, edgerunners2]),
        .shoot("iphone-03"),
        .follow([blackClover, frieren2, dandadan]),
        .notify([edgerunners2]),
        .pause(60),
        .notify([blackClover, steelBallRun]),
        .pause(60),
        .notify([frieren2]),
        .notificationCenter([frieren2, steelBallRun, blackClover, edgerunners2]),
        .shoot("iphone-05"),
        .database(DatabaseEdit(
            newEpisodes: [edgerunners2, blackClover, steelBallRun],
            episodesViewed: [frieren2: 10, blackClover: 4, dandadan: 0]
        )),
        .favorites(top: Anchor(title: frieren2), extraInfo: [frieren2, blackClover, dandadan]),
        .shoot("iphone-06"),
        .database(DatabaseEdit(
            unfollow: [frieren2, dandadan],
            newEpisodes: [edgerunners2, steelBallRun],
            episodesViewed: [blackClover: 0]
        )),
        .favorites(extraInfo: [blackClover]),
        .shoot("iphone-04")
    ]

    /// iPad Pro 13-inch: Main on its side, the rest upright.
    private static let iPadSteps: [ScreenshotStep] = [
        .follow([steelBallRun, frierenGold, mashle, edgerunners, edgerunners2]),
        .turn(.landscapeLeft),
        .onAir(dateMode: [blackClover, kusuriya]),
        .shoot("ipad-01"),
        .soon(top: Anchor(title: bocchi), dateMode: [frierenGold, mashle]),
        .shoot("ipad-02"),
        .turn(.portrait),
        .search("cyberpunk", dateMode: [maou], shows: [edgerunners, edgerunners2]),
        .shoot("ipad-03"),
        .follow([blackClover, kusuriya, dandadan, onePiece, frieren2]),
        .notify([frieren2, blackClover, kusuriya, steelBallRun]),
        .pause(60),
        .notify([onePiece]),
        .pause(60),
        .notify([edgerunners2]),
        .notificationCenter([
            edgerunners2, onePiece, steelBallRun, kusuriya, blackClover, frieren2
        ]),
        .shoot("ipad-05"),
        .database(DatabaseEdit(
            unfollow: [frieren2, onePiece],
            newEpisodes: [edgerunners2, kusuriya, steelBallRun]
        )),
        .favorites(),
        .shoot("ipad-04"),
        .follow([frieren2]),
        .database(DatabaseEdit(
            unfollow: [edgerunners, edgerunners2, frierenGold],
            newEpisodes: [frieren2, blackClover, kusuriya, steelBallRun],
            episodesViewed: [frieren2: 10, blackClover: 4, kusuriya: 4, dandadan: 0]
        )),
        .favorites(extraInfo: [frieren2, blackClover, kusuriya, dandadan]),
        .shoot("ipad-06")
    ]
}
