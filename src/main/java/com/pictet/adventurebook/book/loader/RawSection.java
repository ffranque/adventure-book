package com.pictet.adventurebook.book.loader;

import java.util.List;

public record RawSection(int id, String text, String type, List<RawOption> options) {
}
