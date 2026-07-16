# CypherDocs

**One reader. Every file. No PDF clutter, no editor bloat.**

CypherDocs is a privacy-first, open-source Android app for reading files — text, images,
and office documents (`.docx`, `.xlsx`, `.pptx`, `.odt`, `.ods`, `.odp`) — all in one place.
It's the companion reader to [CypherPDF](https://github.com/prvthmpcypher/CypherPDF):
CypherPDF handles PDFs, CypherDocs handles everything *else*.

## What it is

- **All-in-one reader** for text, source-code, markup, images, and Office/OpenDocument files.
- **Read-only.** There is no editing anywhere in this app, by design.
- **No app-drawer icon — Open-with only.** CypherDocs has no launcher/MAIN activity at all,
  so it never appears in the app drawer or home screen and can't be tapped open like a normal
  app. The *only* way in is through another app's **"Open with…"** or **Share** menu (file
  manager, email attachment, browser download, etc.) — `OpenWithEntryActivity` is the sole
  exported entry point, and it forwards straight to the right viewer.
- **No PDF support**, on purpose — use CypherPDF for PDFs. If a `.pdf` is opened via
  "Open with", CypherDocs still shows up in the chooser (the mime filter is broad by design)
  but just shows a message pointing to CypherPDF instead of opening it.
- **100% offline.** No network permission, no analytics, no ads.

## Features

- 📂 Built-in folder browser (Storage Access Framework) — pick a folder once, browse and
  drill into subfolders from inside the app.
- 🔍 **Search** inside any text-based file, with match highlighting and next/previous jump.
- 🔎 **Zoom** — pinch-to-zoom + double-tap on images, font-size zoom on text/documents.
- 📖 **Page navigation** — flip to the next/previous file in the same folder without going
  back to the file picker.
- 📤 **Share** the currently open file out to any other app.
- 🗂 **Recents** — quickly reopen the last files you read.
- 🖼 Reads images: JPG, PNG, GIF, WEBP, BMP, HEIC/HEIF.
- 📝 Reads text & code: TXT, MD, JSON, XML, CSV, LOG, and most common source file extensions.
- 📄 Reads Office/OpenDocument files by extracting their text content (no external libraries,
  fully offline) — great for reading, not a substitute for full document rendering.

## How to open it

Since there's no app icon, you open CypherDocs the same way you'd pick any other app to
view a file with:

1. In a file manager, email client, browser downloads list, chat app, etc., tap a file →
   **Share** or **Open with**.
2. Pick **CypherDocs** from the chooser.
3. From inside the viewer, tap **Browse files** (top toolbar) any time to reach the
   built-in folder browser and Recents list.

For development, you can also trigger it straight from adb without any other app:

```bash
adb shell am start -a android.intent.action.VIEW \
  -d "content://com.android.externalstorage.documents/document/primary%3ADownload%2Fnotes.txt" \
  -t "text/plain" \
  com.prvthmpcypher.cypherdocs.debug/com.prvthmpcypher.cypherdocs.OpenWithEntryActivity
```

## Tech stack

- Kotlin, native Android Views (no Compose, kept deliberately lightweight)
- Storage Access Framework (`ACTION_OPEN_DOCUMENT_TREE`, `DocumentFile`)
- `FileProvider` for safe sharing
- Zero third-party parsing libraries — Office file text extraction is done with a small
  hand-written ZIP/XML reader (`OfficeXmlExtractor.kt`)
- Min SDK 24, Target/Compile SDK 34

## Building

```bash
git clone https://github.com/prvthmpcypher/CypherDocs.git
cd CypherDocs
gradle wrapper --gradle-version 8.7   # one-time, generates gradlew (not committed)
./gradlew assembleDebug
```

The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

Or just open the project folder in Android Studio (Iguana or newer) — it will offer to
generate the Gradle wrapper for you automatically.

### CI

`.github/workflows/android-build.yml` builds debug + release (unsigned) APKs and runs lint
on every push/PR to `main`, and lets you trigger a manual build from the Actions tab.

## Project layout

```
app/src/main/java/com/prvthmpcypher/cypherdocs/
├── OpenWithEntryActivity.kt         Sole exported activity — receives ACTION_VIEW/SEND
│                                     from another app's "Open with"/Share chooser
├── MainActivity.kt                 Folder browser + recent files (internal only, no launcher)
├── ui/
│   ├── FolderBrowserActivity.kt    Browse a folder's files & subfolders
│   ├── FolderEntriesAdapter.kt
│   └── RecentFilesAdapter.kt
├── viewer/
│   ├── ViewerRouter.kt             Routes a file to the right viewer by type
│   ├── BaseViewerActivity.kt       Shared page-navigation + share logic
│   ├── TextViewerActivity.kt       Text/code viewer: search, zoom, share, "Browse files"
│   ├── ImageViewerActivity.kt      Image viewer: pinch-zoom, share, "Browse files"
│   └── DocViewerActivity.kt        Office/OpenDocument text extraction viewer
├── util/
│   ├── FileTypeUtils.kt            Extension/mime → file-type classification
│   ├── OfficeXmlExtractor.kt       Dependency-free docx/xlsx/pptx/odt/ods/odp text extractor
│   ├── RecentFilesStore.kt         SharedPreferences-backed recents list
│   └── ShareUtils.kt               FileProvider-based sharing
└── widget/
    └── ZoomableImageView.kt        Dependency-free pinch/pan/double-tap ImageView
```

## License

GPL-3.0 — see [LICENSE](LICENSE).

## Logo

Brand SVG at [`logo/cypherdocs_logo.svg`](logo/cypherdocs_logo.svg); the Android adaptive
launcher icon is built from the same design (`app/src/main/res/drawable/ic_launcher_foreground.xml`
+ `ic_launcher_background.xml`).

## More from Poorvith M P

| Project | What it does |
|---|---|
| [CypherPDF](https://github.com/prvthmpcypher/CypherPDF) | Companion reader for PDFs |
| [AiScrubber](https://github.com/prvthmpcypher/aiscrubber) | Client-side privacy scrubber for AI prompts |
| [PaperHive](https://github.com/prvthmpcypher/paperhive) | Offline-first PDF toolkit for the browser |

## Connect

[![GitHub](https://img.shields.io/badge/GitHub-prvthmpcypher-181717?logo=github)](https://github.com/prvthmpcypher)
[![X](https://img.shields.io/badge/X-@poorvithmp07-000000?logo=x)](https://x.com/poorvithmp07)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-poorvithmp-0A66C2?logo=linkedin)](https://www.linkedin.com/in/poorvithmp)
