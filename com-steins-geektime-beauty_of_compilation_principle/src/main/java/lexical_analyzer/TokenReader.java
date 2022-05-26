package lexical_analyzer;

public interface TokenReader {
    // 返回token流中token，从流中读取
    public Token read();

    // 返回token流中token，不从流中读取
    public Token peek();

    // 回退
    public void unread();

    // 返回当前位置
    public int getPosition();

    // 设置读取的当前位置
    public void setPosition(int pos);
}
