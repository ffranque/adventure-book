package com.pictet.adventurebook.book.loader;

import java.util.List;

public record RawBook(String title, String author, String difficulty, List<String> categories,
                      List<RawSection> sections) {
}
