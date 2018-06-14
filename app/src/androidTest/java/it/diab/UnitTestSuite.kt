package it.diab

import it.diab.db.AppDatabaseTest
import it.diab.db.converters.DateConverterTest
import it.diab.db.converters.TimeFrameConverterTest
import it.diab.db.entities.GlucoseTest
import it.diab.glucose.GlucoseViewModelTest
import it.diab.glucose.editor.GlucoseEditorViewModelTest
import it.diab.insulin.editor.InsulinEditorViewModelTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
        AppDatabaseTest::class,
        DateConverterTest::class,
        GlucoseEditorViewModelTest::class,
        GlucoseViewModelTest::class,
        GlucoseTest::class,
        InsulinEditorViewModelTest::class,
        TimeFrameConverterTest::class)
class UnitTestSuite