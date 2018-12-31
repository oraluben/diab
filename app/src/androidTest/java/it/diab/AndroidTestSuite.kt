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
import it.diab.db.entities.GlucoseTest
import it.diab.viewmodels.glucose.OverviewViewModelTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    AppDatabaseTest::class,
    it.diab.viewmodels.glucose.EditorViewModelTest::class,
    GlucoseTest::class,
    it.diab.viewmodels.insulin.EditorViewModelTest::class,
    OverviewViewModelTest::class
)
class AndroidTestSuite