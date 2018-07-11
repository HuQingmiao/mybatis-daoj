package your.project.demo.vo;

import your.project.demo.common.BasicVo;

public class BookEditor extends BasicVo {
    private static final long serialVersionUID = 1L;

    private Long bookId;
    private Long editorId;


    public Long getBookId() {
        return bookId;
   }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
   }

    public Long getEditorId() {
        return editorId;
   }

    public void setEditorId(Long editorId) {
        this.editorId = editorId;
   }

}

/*List columns as follows:
"book_id", "editor_id"
*/