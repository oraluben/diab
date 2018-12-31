/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab

import it.diab.db.converters.DateConverterTest
import it.diab.db.converters.TimeFrameConverter
import it.diab.util.extensions.DateExtTest
import it.diab.util.extensions.FloatExtTest
import it.diab.util.extensions.StringExtTest
import org.junit.runners.Suite

@Suite.SuiteClasses(
        DateConverterTest::class,
        DateExtTest::class,
        FloatExtTest::class,
        StringExtTest::class,
        TimeFrameConverter::class)
class UnitTestSuite