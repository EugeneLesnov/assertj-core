/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2012-2021 the original author or authors.
 */
package org.assertj.core.internal.paths;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.error.ShouldBeReadable.shouldBeReadable;
import static org.assertj.core.error.ShouldExist.shouldExist;
import static org.assertj.core.error.ShouldHaveBinaryContent.shouldHaveBinaryContent;
import static org.assertj.core.util.AssertionsUtil.expectAssertionError;
import static org.assertj.core.util.FailureMessages.actualIsNull;
import static org.junit.jupiter.api.condition.OS.WINDOWS;
import static org.mockito.BDDMockito.given;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.assertj.core.internal.BinaryDiffResult;
import org.assertj.core.internal.PathsBaseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;

class Paths_assertHasSameBinaryContentAs_Test extends PathsBaseTest {

  @Test
  void should_fail_if_expected_is_null() throws IOException {
    // GIVEN
    Path actual = Files.createFile(tempDir.resolve("actual"));
    // WHEN
    Throwable thrown = catchThrowable(() -> paths.assertHasSameBinaryContentAs(info, actual, null));
    // THEN
    then(thrown).isInstanceOf(NullPointerException.class)
                .hasMessage("The given Path to compare actual content to should not be null");
  }

  @Test
  void should_fail_if_expected_does_not_exist() throws IOException {
    // GIVEN
    Path actual = Files.createFile(tempDir.resolve("actual"));
    Path expected = tempDir.resolve("non-existent");
    // WHEN
    Throwable thrown = catchThrowable(() -> paths.assertHasSameBinaryContentAs(info, actual, expected));
    // THEN
    then(thrown).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The given Path <%s> to compare actual content to should exist", expected);
  }

  @Test
  @DisabledOnOs(WINDOWS)
  void should_fail_on_unix_if_expected_is_not_readable() throws IOException {
    // GIVEN
    Path actual = Files.createFile(tempDir.resolve("actual"));
    Path expected = Files.createFile(tempDir.resolve("expected"));
    expected.toFile().setReadable(false);
    // WHEN
    Throwable thrown = catchThrowable(() -> paths.assertHasSameBinaryContentAs(info, actual, expected));
    // THEN
    then(thrown).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The given Path <%s> to compare actual content to should be readable", expected);
  }

  @Test
  void should_fail_if_actual_is_null() throws IOException {
    // GIVEN
    Path expected = Files.createFile(tempDir.resolve("expected"));
    // WHEN
    AssertionError error = expectAssertionError(() -> paths.assertHasSameBinaryContentAs(info, null, expected));
    // THEN
    then(error).hasMessage(actualIsNull());
  }

  @Test
  void should_fail_if_actual_does_not_exist() throws IOException {
    // GIVEN
    Path actual = tempDir.resolve("non-existent");
    Path expected = Files.createFile(tempDir.resolve("expected"));
    // WHEN
    AssertionError error = expectAssertionError(() -> paths.assertHasSameBinaryContentAs(info, actual, expected));
    // THEN
    then(error).hasMessage(shouldExist(actual).create());
  }

  @Test
  @DisabledOnOs(WINDOWS)
  void should_fail_on_unix_if_actual_is_not_readable() throws IOException {
    // GIVEN
    Path actual = Files.createFile(tempDir.resolve("actual"));
    actual.toFile().setReadable(false);
    Path expected = Files.createFile(tempDir.resolve("expected"));
    // WHEN
    AssertionError error = expectAssertionError(() -> paths.assertHasSameBinaryContentAs(info, actual, expected));
    // THEN
    then(error).hasMessage(shouldBeReadable(actual).create());
  }

  // FIXME add cases with different encoding
  @Test
  void should_pass_if_actual_has_the_same_binary_content_as_expected() throws IOException {
    // GIVEN
    Path actual = Files.write(tempDir.resolve("actual"), "Content".getBytes());
    Path expected = Files.write(tempDir.resolve("expected"), "Content".getBytes());
    // WHEN/THEN
    paths.assertHasSameBinaryContentAs(info, actual, expected);
  }

  // FIXME add cases with different encoding
  @Test
  void should_fail_if_actual_does_not_have_the_same_binary_content_as_expected() throws IOException {
    // GIVEN
    Path actual = Files.write(tempDir.resolve("actual"), "Content".getBytes());
    Path expected = Files.write(tempDir.resolve("expected"), "Another content".getBytes());
    BinaryDiffResult diff = binaryDiff.diff(actual, "Another content".getBytes());
    // WHEN
    AssertionError error = expectAssertionError(() -> paths.assertHasSameBinaryContentAs(info, actual, expected));
    // THEN
    then(error).hasMessage(shouldHaveBinaryContent(actual, diff).create(info.description(), info.representation()));
  }

  @Test
  void should_rethrow_IOException_as_UncheckedIOException() throws IOException {
    // GIVEN
    Path actual = Files.write(tempDir.resolve("actual"), "Content".getBytes());
    Path expected = Files.write(tempDir.resolve("expected"), "Content".getBytes());
    IOException exception = new IOException("boom!");
    given(binaryDiff.diff(actual, "Content".getBytes())).willThrow(exception);
    // WHEN
    Throwable thrown = catchThrowable(() -> paths.assertHasSameBinaryContentAs(info, actual, expected));
    // THEN
    then(thrown).isInstanceOf(UncheckedIOException.class)
                .hasCause(exception);
  }

}
