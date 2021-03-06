package com.iloc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static com.iloc.TokenType.*;

class Parser {
    private final List<Token> tokens;
    private final List<Operators> opsList;
    private int current = 0;
    private boolean hasError = false;

    Parser(List<Token> tokens) {
        opsList = new ArrayList<>();
        this.tokens = tokens;
    }

    List<Operators> parse() {
        while (!isAtEnd()) {
            switch (peek().type) {
                case ADD:
                case SUB:
                case MULT:
                case RSHIFT:
                case LSHIFT:
                    addInstruction(arithmetic_op());
                    break;
                case LOAD:
                case STORE:
                    addInstruction(load_store_op());
                    break;
                case LOADI:
                    addInstruction(loadi_op());
                    break;
                case OUTPUT:
                    addInstruction(output_op());
                    break;
                case NOP:
                    addInstruction(nop());
                    break;
                default:
                    reportError("Unexpected token");
                    break;
            }
        }
        return opsList;
    }

    private Operators output_op() {
        Token operator = advance();
        Token first = consume(NUMBER, "Expected number but found " + peek().lexeme);
        if (first.type == ERROR) return null;
        return new Operators.Unary(operator, first);
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        reportError(message);
        return new Token(ERROR, peek().lexeme, peek().line);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    boolean hasError() {
        return hasError;
    }

    private void addInstruction(Operators op) {
        if (op != null) opsList.add(op);
    }

    private void reportError(String message) {
        hasError = true;
        Main.error(peek(), message);
        advance();
    }

     private Operators arithmetic_op() {
        Token operator = advance();
        Token first = consume(REGISTER, "Expected source register 1 but found " + peek().lexeme);
        consume(COMMA, "Expected a comma after register name");
        Token second = consume(REGISTER, "Expected source register 2 but found " + peek().lexeme);
        consume(ARROW, "Expected => after register name");
        Token third = consume(REGISTER, "Expected destination register but found " + peek().lexeme);
        if (first.type == ERROR || second.type == ERROR || third.type == ERROR) return null;
        return new Operators.Ternary(operator, first, second, third);
    }

    private Operators nop() {
        Token operator = advance();
        return new Operators.None(operator);
    }

    private Operators load_store_op() {
        Token operator = advance();
        Token first = consume(REGISTER, "Expected source register but found " + peek().lexeme);
        consume(ARROW, "Expected => after register name");
        Token second = consume(REGISTER, "Expected destination register but found " + peek().lexeme);
        if (first.type == ERROR || second.type == ERROR) return null;
        return new Operators.Binary(operator, first, second);
    }

    private Operators loadi_op() {
        Token operator = advance();
        Token first = consume(NUMBER, "Expected number but found " + peek().lexeme);
        consume(ARROW, "Expected => after register name");
        Token second = consume(REGISTER, "Expected destination register but found" + peek().lexeme);
        if (first.type == ERROR || second.type == ERROR) return null;
        return new Operators.Binary(operator, first, second);
    }
}
