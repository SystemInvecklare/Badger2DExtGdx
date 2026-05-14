package com.github.systeminvecklare.badger.impl.gdx.util;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

public class StrictJsonParser {
	private final char[] characters;
	private int nextIndex = 0;

	public StrictJsonParser(String string) {
			this.characters = string.toCharArray();
		}

	private char next() {
		if (nextIndex >= characters.length) {
			throw new RuntimeException("End of stream");
		}
		return characters[nextIndex++];
	}

	private char nextSkipWhitespace() {
		char c = next();
		while (Character.isWhitespace(c)) {
			c = next();
		}
		return c;
	}

	public JsonValue parse() {
		char c = nextSkipWhitespace();
		if (c == '{') {
			return parseObject();
		} else if (c == '[') {
			return parseArray();
		} else if (c == '"') {
			return new JsonValue(parseString());
		} else if (isDigit(c) || c == '-') {
			nextIndex--;
			return parseNumber();
		} else if (isExactIfSoSkip(c, "true")) {
			return new JsonValue(true);
		} else if (isExactIfSoSkip(c, "false")) {
			return new JsonValue(false);
		} else if (isExactIfSoSkip(c, "null")) {
			return new JsonValue(JsonValue.ValueType.nullValue);
		} else {
			unexpectedCharacter(c, "'{', '[', '\"', '0'-'9', '-', true, false or null");
			return null; // Should never happen
		}
	}

	private boolean isExactIfSoSkip(char first, String target) {
		if (first != target.charAt(0)) {
			return false;
		}
		// need nextIndex + target.length() - 2 < character.length()
		// fail if nextIndex + target.length() - 2 >= character.length()
		// fail if nextIndex + target.length() - 1 > character.length()
		if (nextIndex + target.length() - 1 > characters.length) {
			return false;
		}
		for (int i = 1; i < target.length(); ++i) {
			if (characters[nextIndex + i - 1] != target.charAt(i)) {
				return false;
			}
		}
		nextIndex += target.length() - 1;
		return true;
	}

	private char parseDigits(StringBuilder numberString) {
		char c;
		while (isDigit(c = next())) {
			numberString.append(c);
		}
		return c;
	}

	private JsonValue parseNumber() {
		boolean hasFraction = false;
		StringBuilder numberStringBuilder = new StringBuilder();
		char c = next();
		if (c == '-') {
			numberStringBuilder.append(c);
			c = next();
		}
		nextIndex--;
		c = parseDigits(numberStringBuilder);
		if (c == '.') {
			hasFraction = true;
			numberStringBuilder.append(c);
			c = parseDigits(numberStringBuilder);
		}
		if (c == 'e' || c == 'E') {
			numberStringBuilder.append(c);
			c = next();
			if (c == '+' || c == '-') {
				if (c == '-') {
					hasFraction = true;
				}
				numberStringBuilder.append(c);
			} else {
				nextIndex--;
			}
			c = parseDigits(numberStringBuilder);
		}
		nextIndex--;
		String numberString = numberStringBuilder.toString();
		if (hasFraction) {
			double dubs = Double.parseDouble(numberString);
			return new JsonValue(dubs, numberString);
		} else {
			long longs = Long.parseLong(numberString);
			return new JsonValue(longs, numberString);
		}
	}

	private boolean isDigit(char c) {
		return (c >= '0' && c <= '9');
	}

	private JsonValue parseObject() {
		char c = nextSkipWhitespace();
		JsonValue object = new JsonValue(ValueType.object);
		if (c == '}') {
			return object;
		}
		nextIndex--;
		c = ',';
		while (c == ',') {
			parseEntry(object);
			c = nextSkipWhitespace();
		}
		if (c != '}') {
			unexpectedCharacter(c, "'}'");
		}
		return object;
	}

	private JsonValue parseArray() {
		char c = nextSkipWhitespace();
		JsonValue array = new JsonValue(ValueType.array);
		if (c == ']') {
			return array;
		}
		nextIndex--;
		c = ',';
		while (c == ',') {
			array.addChild(parse());
			c = nextSkipWhitespace();
		}
		if (c != ']') {
			unexpectedCharacter(c, "']'");
		}
		return array;
	}

	private void parseEntry(JsonValue object) {
		char c = nextSkipWhitespace();
		if (c != '"') {
			unexpectedCharacter(c, "\"");
		}
		String name = parseString();
		c = nextSkipWhitespace();
		if (c != ':') {
			unexpectedCharacter(':', "':'");
		}
		JsonValue value = parse();
		object.addChild(name, value);
	}

	private String parseString() {
		StringBuilder builder = new StringBuilder();
		char c;
		while ((c = next()) != '"') {
			if (c == '\\') {
				c = next();
				if (c == 'u') {
					int codePoint = 0;
					for (int i = 0; i < 4; i++) {
						char hex = next();
						codePoint <<= 4;
						if (hex >= '0' && hex <= '9')
							codePoint += hex - '0';
						else if (hex >= 'a' && hex <= 'f')
							codePoint += hex - 'a' + 10;
						else if (hex >= 'A' && hex <= 'F')
							codePoint += hex - 'A' + 10;
						else
							throw new RuntimeException("Invalid hex digit: " + hex);
					}
					builder.append((char) codePoint);
				} else {
					builder.append(c);
				}
			} else {
				builder.append(c);
			}
		}
		return builder.toString();
	}

	private void unexpectedCharacter(char c, String expected) {
		int start = Math.max(0, nextIndex - 1 - 10);
		int end = Math.min(characters.length - 1, nextIndex + 10);
		String near = null;
		if (end - start >= 0) {
			near = new String(characters, start, end - start);
		}
		int row = 1;
		int column = 1;
		int columnRaw = 1;
		for (int i = 0; i < nextIndex - 1; ++i) {
			if (characters[i] == '\n') {
				row++;
				column = 1;
				columnRaw = 1;
			} else {
				if (characters[i] == '\t') {
					column += 4;
				} else {
					column++;
				}
				columnRaw++;
			}
		}
		throw new RuntimeException("Unexpected character at " + row + ":" + column + "(character number " + columnRaw
				+ "): --> " + c + " <-- (expected " + expected + ") near " + near);
	}
}
