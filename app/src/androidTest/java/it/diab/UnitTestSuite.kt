/*
 * Copyright (c) 2018 Bevilacqua Joey
 *
 * Licensed under the GNU GPLv3 license
 *
 * The text of the license can be found in the LICENSE file
 * or at https://www.gnu.org/licenses/gpl.txt
 */
package it.diab

import it.diab.db.AppDatabaseTest
import it.diab.db.converters.DateConverterTest
import it.diab.db.converters.TimeFrameConverterTest
import it.diab.db.entities.GlucoseTest
import it.diab.glucose.editor.GlucoseEditorViewModelTest
import it.diab.glucose.overview.OverviewViewModelTest
import it.diab.insulin.editor.InsulinEditorViewModelTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
        AppDatabaseTest::class,
        DateConverterTest::class,
        GlucoseEditorViewModelTest::class,
        GlucoseTest::class,
        InsulinEditorViewModelTest::class,
        OverviewViewModelTest::class,
        TimeFrameConverterTest::class)
class UnitTestSuite