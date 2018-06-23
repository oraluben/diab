package it.diab

import it.diab.db.AppDatabaseTest
import it.diab.db.converters.DateConverterTest
import it.diab.db.converters.TimeFrameConverterTest
import it.diab.db.entities.GlucoseTest
import it.diab.glucose.editor.GlucoseEditorViewModelTest
import it.diab.glucose.list.GlucoseListViewModelTest
import it.diab.glucose.overview.OverviewViewModelTest
import it.diab.insulin.editor.InsulinEditorViewModelTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
        AppDatabaseTest::class,
        DateConverterTest::class,
        GlucoseEditorViewModelTest::class,
        GlucoseListViewModelTest::class,
        GlucoseTest::class,
        InsulinEditorViewModelTest::class,
        OverviewViewModelTest::class,
        TimeFrameConverterTest::class)
class UnitTestSuite