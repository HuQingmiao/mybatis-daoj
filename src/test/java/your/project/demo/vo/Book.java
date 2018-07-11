package your.project.demo.vo;

import your.project.demo.common.BasicVo;

public class Book extends BasicVo {
    private static final long serialVersionUID = 1L;

    private Long bookId;
    private String title;
    private Double price;
    private java.sql.Timestamp publishTime;
    private byte[] blobContent;
    private String textContent;

    //多表查询关联的列
    private Long editorId;
    private String editorName;
    private String editorSex;

    public Long getBookId() {
        return bookId;
   }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
   }

    public String getTitle() {
        return title;
   }

    public void setTitle(String title) {
        this.title = title;
   }

    public Double getPrice() {
        return price;
   }

    public void setPrice(Double price) {
        this.price = price;
   }

    public java.sql.Timestamp getPublishTime() {
        return publishTime;
   }

    public void setPublishTime(java.sql.Timestamp publishTime) {
        this.publishTime = publishTime;
   }

    public byte[] getBlobContent() {
        return blobContent;
   }

    public void setBlobContent(byte[] blobContent) {
        this.blobContent = blobContent;
   }

    public String getTextContent() {
        return textContent;
   }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
   }

    public String getEditorSex() {
        return editorSex;
    }

    public void setEditorSex(String editorSex) {
        this.editorSex = editorSex;
    }

    public String getEditorName() {
        return editorName;
    }

    public void setEditorName(String editorName) {
        this.editorName = editorName;
    }

    public Long getEditorId() {
        return editorId;
    }

    public void setEditorId(Long editorId) {
        this.editorId = editorId;
    }
}

/*List columns as follows:
"book_id", "title", "price", "publish_time", "blob_content", "text_content"
*/