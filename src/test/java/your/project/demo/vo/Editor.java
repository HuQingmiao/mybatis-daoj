package your.project.demo.vo;

import your.project.demo.common.BasicVo;

public class Editor extends BasicVo {
    private static final long serialVersionUID = 1L;

    private Long editorId;
    private String name;
    private String sex;


    public Long getEditorId() {
        return editorId;
   }

    public void setEditorId(Long editorId) {
        this.editorId = editorId;
   }

    public String getName() {
        return name;
   }

    public void setName(String name) {
        this.name = name;
   }

    public String getSex() {
        return sex;
   }

    public void setSex(String sex) {
        this.sex = sex;
   }

}

/*List columns as follows:
"editor_id", "name", "sex"
*/