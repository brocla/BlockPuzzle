"""
Generates an Android vector drawable XML for the Block Puzzle app icon.

The icon is a pattern of rounded-square blocks arranged in a grid.
Edit the BLOCKS list to change which cells are filled and their colors.

Usage:
    python icon_gen.py                          # prints to stdout
    python icon_gen.py -o app/src/main/res/drawable/ic_block_puzzle.xml
"""

import argparse


def generate_block_path(x, y, size=8, radius=2):
    """Generates the SVG/XML path string for a rounded square."""
    return (
        f"M{x+radius},{y} "
        f"h{size-2*radius} "
        f"a{radius},{radius} 0 0 1 {radius},{radius} "
        f"v{size-2*radius} "
        f"a{radius},{radius} 0 0 1 -{radius},{radius} "
        f"h-{size-2*radius} "
        f"a{radius},{radius} 0 0 1 -{radius},-{radius} "
        f"v-{size-2*radius} "
        f"a{radius},{radius} 0 0 1 {radius},-{radius} z"
    )


# ── Icon configuration ──

VIEWPORT = 24          # viewportWidth/Height
ICON_SIZE = 108        # width/height in dp
BLOCK_SIZE = 4         # size of each block in viewport units
CORNER_RADIUS = 1      # rounded corner radius
STROKE_COLOR = "#2C2C2C"
STROKE_WIDTH = 1

# Each block: (x, y, fill_color)
# Coordinates are in viewport units (0-24).
BLOCKS = [
    (12,  6, "#FFD700"),   # top-right
    ( 8, 10, "#FFD700"),   # middle-left
    (12, 10, "#FFD700"),   # middle-right
    ( 8, 14, "#FFD700"),   # bottom-left
]


def generate_icon_xml():
    """Generates the complete Android vector drawable XML."""
    lines = [
        f'<vector xmlns:android="http://schemas.android.com/apk/res/android"',
        f'    android:width="{ICON_SIZE}dp"',
        f'    android:height="{ICON_SIZE}dp"',
        f'    android:viewportWidth="{VIEWPORT}"',
        f'    android:viewportHeight="{VIEWPORT}">',
    ]

    for x, y, color in BLOCKS:
        path = generate_block_path(x, y, size=BLOCK_SIZE, radius=CORNER_RADIUS)
        lines.append("")
        lines.append("    <path")
        lines.append(f'        android:fillColor="{color}"')
        lines.append(f'        android:strokeColor="{STROKE_COLOR}"')
        lines.append(f'        android:strokeWidth="{STROKE_WIDTH}"')
        lines.append(f'        android:pathData="{path}" />')

    lines.append("")
    lines.append("</vector>")
    return "\n".join(lines)


def main():
    parser = argparse.ArgumentParser(description="Generate Block Puzzle app icon XML")
    parser.add_argument("-o", "--output", help="Output file path (default: stdout)")
    args = parser.parse_args()

    xml = generate_icon_xml()

    if args.output:
        with open(args.output, "w", encoding="utf-8") as f:
            f.write(xml + "\n")
        print(f"Wrote {args.output}")
    else:
        print(xml)


if __name__ == "__main__":
    main()
