package entity;

import java.util.List;

/**
 *
 * @author Son
 */
public class CatMenu {
    private String text;
    private byte[] iv;
    private List<ItemMenu> items;

    public CatMenu() {
    }
    
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public byte[] getIv() {
        return iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }

    public List<ItemMenu> getItems() {
        return items;
    }

    public void setItems(List<ItemMenu> items) {
        this.items = items;
    }
    
    
}
