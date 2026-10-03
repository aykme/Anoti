"""Fails when a part of a screenshot that the app keeps dark in every system theme is light.

Modes: `whole` for the launch, `edges` for the top system bar and the area under the bottom
navigation, `center` for the app's card in the app switcher."""
import struct
import sys
import zlib

EDGE_FRACTION = 0.03  # the top and bottom 3% of the screen
MAX_MEAN_BRIGHTNESS = 60  # out of 255; the app's bars are black or near it


def read_png(path):
    with open(path, "rb") as file:
        data = file.read()
    assert data[:8] == b"\x89PNG\r\n\x1a\n", "not a PNG"
    pos, chunks, header = 8, [], None
    while pos < len(data):
        length, kind = struct.unpack(">I4s", data[pos:pos + 8])
        body = data[pos + 8:pos + 8 + length]
        if kind == b"IHDR":
            header = struct.unpack(">IIBBBBB", body)
        elif kind == b"IDAT":
            chunks.append(body)
        pos += 12 + length
    width, height, depth, color, _, _, interlace = header
    print(f"{path}: {width}x{height}, depth {depth}, color type {color}")
    assert depth in (8, 16) and interlace == 0 and color in (2, 6), f"unsupported PNG {header}"
    channels = 4 if color == 6 else 3
    bpp = channels * depth // 8  # bytes per pixel, the offset the filters work with
    raw = zlib.decompress(b"".join(chunks))
    stride = width * bpp
    rows, previous, pos = [], bytearray(stride), 0
    for _ in range(height):
        kind, line = raw[pos], bytearray(raw[pos + 1:pos + 1 + stride])
        pos += 1 + stride
        for i in range(stride):
            left = line[i - bpp] if i >= bpp else 0
            up = previous[i]
            corner = previous[i - bpp] if i >= bpp else 0
            if kind == 1:
                line[i] = (line[i] + left) & 0xFF
            elif kind == 2:
                line[i] = (line[i] + up) & 0xFF
            elif kind == 3:
                line[i] = (line[i] + (left + up) // 2) & 0xFF
            elif kind == 4:
                guess = left + up - corner
                pa, pb, pc = abs(guess - left), abs(guess - up), abs(guess - corner)
                nearest = left if pa <= pb and pa <= pc else up if pb <= pc else corner
                line[i] = (line[i] + nearest) & 0xFF
        # A 16-bit sample is judged by its high byte.
        rows.append(line if depth == 8 else line[0::2])
        previous = line
    return width, height, channels, rows


def mean_brightness(rows, channels):
    total = count = 0
    for line in rows:
        for i in range(0, len(line), channels):
            total += (line[i] + line[i + 1] + line[i + 2]) / 3
            count += 1
    return total / count


def crop(rows, channels, top, bottom, left, right):
    """The rows from fraction top to bottom, and in each the pixels from left to right."""
    height, width = len(rows), len(rows[0]) // channels
    first_col, last_col = int(width * left), max(int(width * left) + 1, int(width * right))
    return [
        line[first_col * channels:last_col * channels]
        for line in rows[int(height * top):max(int(height * top) + 1, int(height * bottom))]
    ]


def main(mode, path):
    _, _, channels, rows = read_png(path)
    if mode == "whole":
        parts = {"whole": crop(rows, channels, 0, 1, 0, 1)}
    elif mode == "edges":
        parts = {
            "top": crop(rows, channels, 0, EDGE_FRACTION, 0, 1),
            "bottom": crop(rows, channels, 1 - EDGE_FRACTION, 1, 0, 1),
        }
    elif mode == "center":
        parts = {"center": crop(rows, channels, 0.35, 0.65, 0.35, 0.65)}
    else:
        sys.exit(f"unknown mode {mode}")
    values = {name: mean_brightness(part, channels) for name, part in parts.items()}
    shown = ", ".join(f"{name} {value:.0f}" for name, value in values.items())
    print(f"{path}: {shown} (limit {MAX_MEAN_BRIGHTNESS})")
    return 0 if all(value <= MAX_MEAN_BRIGHTNESS for value in values.values()) else 1


if __name__ == "__main__":
    sys.exit(main(sys.argv[1], sys.argv[2]))
