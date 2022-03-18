# Time sensitive PDF

This PDF is time sensitive and changes the visible content between 7:00 and 18:00 if the PDF application allows JavaScript (e.g. Acrobat Reader).
If you sign this PDF, the signature stays valid even though the content can change. This is because the integrity of the actual file stays intact, it's just the dynamic content that changes.

To try this PoC yourself, I recommend to open this PDF in Acrobat Reader DC, sign it, and then wait till 7:00 or 18:00 (or you can just change your system clock :) ). When the visible content eventually changes the signature is still valid, and Acrobat shows that the document was not changed since it was signed.

The only way to spot this is by clicking on "Review" when signing the document. There Acrobat will show the warning "Document contains hidden behaviour".
However, this warning is also shown in many other, non-malicious PDFs, for example if they contain clickable links.

The PDF was created with PDFBox 2.0.25, the source code is attached.
