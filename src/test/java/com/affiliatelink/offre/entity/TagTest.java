package com.affiliatelink.offre.entity;

import com.affiliatelink.common.exception.offre.TagInvalideException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TagTest {

    @Test
    void shouldNormalizeTagName() {
        Tag tag = new Tag("  JaVa  ");
        assertEquals("java", tag.getName());
    }

    @Test
    void shouldRemoveDuplicateTagNamesUsingEquality() {
        Set<Tag> tags = new HashSet<>();
        tags.add(new Tag("JAVA"));
        tags.add(new Tag(" java "));
        assertEquals(1, tags.size());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void shouldRejectMissingTagName(String name) {
        assertThrows(TagInvalideException.class, () -> new Tag(name));
    }

    @Test
    void shouldRejectTagNameLongerThan255Characters() {
        assertThrows(TagInvalideException.class, () -> new Tag("x".repeat(256)));
    }
}
