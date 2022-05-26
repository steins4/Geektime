package lexical_analyzer;

public interface Token {
    // 获得token类型：标识符，字面量，关键字
    public TokenType getType();

    // 获得token文本
    public String getTokenText();
}
