package com.prvthmpcypher.cypherdocs

import android.app.Application

/**
 * CypherDocs is a standalone, privacy-first, all-in-one document reader.
 *
 * By design it:
 *  - Reads every common file type EXCEPT PDF (PDF has its own dedicated reader, CypherPDF).
 *  - Never registers itself as a system file handler / "Open with" target.
 *  - Does not include any editing capability — it is a pure reader.
 *  - Does no network access; everything happens on-device.
 */
class CypherDocsApp : Application()
