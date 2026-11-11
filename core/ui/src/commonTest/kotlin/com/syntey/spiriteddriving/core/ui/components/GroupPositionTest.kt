package com.syntey.spiriteddriving.core.ui.components

import kotlin.test.Test
import kotlin.test.assertEquals

class GroupPositionTest {

    @Test
    fun `a single row is alone in its group`() {
        assertEquals(GroupPosition.Alone, GroupPosition.of(index = 0, count = 1))
    }

    @Test
    fun `the first of three rows is first`() {
        assertEquals(GroupPosition.First, GroupPosition.of(index = 0, count = 3))
    }

    @Test
    fun `a row between the first and the last is middle`() {
        assertEquals(GroupPosition.Middle, GroupPosition.of(index = 1, count = 3))
    }

    @Test
    fun `the last of three rows is last`() {
        assertEquals(GroupPosition.Last, GroupPosition.of(index = 2, count = 3))
    }
}
