/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.lang.primitive;

import org.junit.Test;
import r.lang.EvalTestCase;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TextTest extends EvalTestCase {

  @Test
  public void simplePaste() {
      assertThat( eval( ".Internal(paste(list(1, 'a', 'b'), '-', NULL)) "), equalTo(  c("1-a-b") )) ;
  }

  @Test
  public void pasteVectors() {
      assertThat( eval( ".Internal(paste(list(c('x', 'y'), 'a', 'b'), '-', NULL)) "),
          equalTo(  c("x-a-b", "y-a-b") )) ;
  }

  @Test
  public void pasteCollapse() {
      assertThat( eval( ".Internal(paste(list(c('x', 'y'), 'a', 'b'), '-', '+')) "),
          equalTo(  c("x-a-b+y-a-b") )) ;
  }

  @Test
  public void gettext() {
     assertThat( eval( ".Internal(gettext('hungarian', 'hello world'))"), equalTo( c("hello world")));
     assertThat( eval( ".Internal(gettext(NULL, 'hello world'))"), equalTo( c("hello world")));
  }

  @Test
  public void ngettext() {
      assertThat( eval( ".Internal(ngettext(1, 'baby', 'babies', 'hungarian'))"), equalTo( c("baby")));
      assertThat( eval( ".Internal(ngettext(1, 'baby', 'babies', NULL))"), equalTo( c("baby")));
  }

}