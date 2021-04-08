package com.lolson.encryptsms

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    MainActivityWithEncryptTest::class,
    FabActivityTest::class,
    AboutActivityTest::class

)
class TestSuite

//Run all test in this category
