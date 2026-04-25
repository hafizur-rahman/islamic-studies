# Arabic Study Java GUI Application

A Java Swing application with:
- Left pane:
  - Translation `JTextArea` (editable)
  - Arabic verbs table (`JTable`) editable
  - Arabic nouns table (`JTable`) editable
- Right pane:
  - PDF viewer using Apache PDFBox
  - Navigation buttons (next/previous page)
- Local SQLite database storage (`arabic-study.db`)

## Dependencies

- sqlite-jdbc (example: org.xerial:sqlite-jdbc:3.39.3.0)
- Apache PDFBox (example: org.apache.pdfbox:pdfbox:3.0.1)

## Run with command line

1. Download jars and place into `lib/`:
   - `sqlite-jdbc-3.39.3.0.jar`
   - `pdfbox-3.0.1.jar`
   - `commons-logging-1.2.jar` (PDFBox dependency)
   - `fontbox-3.0.1.jar` (PDFBox dependency)

2. Compile:

```powershell
cd c:\Users\bibagimon\work\arabic-study
javac -cp "lib/*" ArabicStudyApp.java
```

3. Run:

```powershell
java -cp ".;lib/*" ArabicStudyApp
```

## Notes

- The DB file `arabic-study.db` is created automatically.
- Use Save Data button to persist table/translation updates.
- PDF load uses file chooser.
