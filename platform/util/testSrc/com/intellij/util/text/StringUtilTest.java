/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.util.text;

import com.intellij.openapi.util.text.StringUtil;
import junit.framework.TestCase;

import java.util.*;

/**
 * @author Eugene Zhuravlev
 *         Date: Dec 22, 2006
 */
public class StringUtilTest extends TestCase {
  public void testToUpperCase() {
    assertEquals('/', StringUtil.toUpperCase('/'));
    assertEquals(':', StringUtil.toUpperCase(':'));
    assertEquals('A', StringUtil.toUpperCase('a'));
    assertEquals('A', StringUtil.toUpperCase('A'));
    assertEquals('K', StringUtil.toUpperCase('k'));
    assertEquals('K', StringUtil.toUpperCase('K'));

    assertEquals('\u2567', StringUtil.toUpperCase(Character.toLowerCase('\u2567')));
  }

  public void testToLowerCase() {
    assertEquals('/', StringUtil.toLowerCase('/'));
    assertEquals(':', StringUtil.toLowerCase(':'));
    assertEquals('a', StringUtil.toLowerCase('a'));
    assertEquals('a', StringUtil.toLowerCase('A'));
    assertEquals('k', StringUtil.toLowerCase('k'));
    assertEquals('k', StringUtil.toLowerCase('K'));

    assertEquals('\u2567', StringUtil.toUpperCase(Character.toLowerCase('\u2567')));
  }

  public void testSplitWithQuotes() {
    final List<String> strings = StringUtil.splitHonorQuotes("aaa bbb   ccc \"ddd\" \"e\\\"e\\\"e\"  ", ' ');
    assertEquals(5, strings.size());
    assertEquals("aaa", strings.get(0));
    assertEquals("bbb", strings.get(1));
    assertEquals("ccc", strings.get(2));
    assertEquals("\"ddd\"", strings.get(3));
    assertEquals("\"e\\\"e\\\"e\"", strings.get(4));
  }

  public void testUnpluralize() {
    assertEquals("s", StringUtil.unpluralize("s"));
    assertEquals("z", StringUtil.unpluralize("zs"));
  }

  public void testStartsWithConcatenation() {
    assertTrue(StringUtil.startsWithConcatenationOf("something.withdot", "something", "."));
    assertTrue(StringUtil.startsWithConcatenationOf("something.withdot", "", "something."));
    assertTrue(StringUtil.startsWithConcatenationOf("something.", "something", "."));
    assertTrue(StringUtil.startsWithConcatenationOf("something", "something", ""));
    assertFalse(StringUtil.startsWithConcatenationOf("something", "something", "."));
    assertFalse(StringUtil.startsWithConcatenationOf("some", "something", ""));
  }

  public void testNaturalCompare() {
    final List<String> strings = new ArrayList(Arrays.asList("Test99", "tes0", "test0", "testing", "test", "test99", "test011", "test1",
                                                             "test 3", "test2", "test10a", "test10", "1.2.10.5", "1.2.9.1"));
    final Comparator<String> c = new Comparator<String>() {
      @Override
      public int compare(String o1, String o2) {
        return StringUtil.naturalCompare(o1, o2);
      }
    };
    Collections.sort(strings, c);
    assertEquals(Arrays.asList("1.2.9.1", "1.2.10.5", "tes0", "test", "test 3", "test0", "test1", "test2", "test10", "test10a",
                               "test011", "Test99", "test99", "testing"), strings);
    final List<String> strings2 = new ArrayList(Arrays.asList("t1", "T2", "T1", "t2"));
    Collections.sort(strings2, c);
    assertEquals(Arrays.asList("T1", "t1", "T2", "t2"), strings2);
  }
}
