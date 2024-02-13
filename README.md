# PDF Annotator: Project Specification

```
kotlinc-jvm 1.8.21-release-380 (JRE 1.8.0_371-b11)
Windows 11 Pro 64-bit
```

## Supported Activities
- Navigate: Browse forward and backwards through the pages in the document. The status bar updates to indicate the current page.
- Drawing: The user can draw on the current page, allowing them to write notes or draw on a page. The user cannot change the color or thickness of the line.
- Highlighting: The user can draw over the existing document with a thick, transparent yellow brush that allows the user to highlight the text in the PDF.
- Erase: The user is able to erase an existing drawing or highlighting.
- Zoom & Pan: The user can use two fingers to zoom-in and zoom-out (by bringing their fingers closer together or spreading
them apart over the area of interest). When zoomed-in, users can pan around to reposition the document. These gestures behave the same as standard pan-and-zoom. Users can draw and highlight a document at any scale, and the annotations scale with the document canvas.
- Undo/Redo: Undo the last change that was made to the document. The user should be able to undo at least the last 5 actions that were performed. Redo can be used to revert the undo.