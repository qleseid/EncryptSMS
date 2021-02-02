package com.example.encryptsms

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    MainActivityTest::class,
    TitleActivityTest::class,
    ItemActivityTest::class,
    AboutActivityTest::class

)
class TestSuite

//Run all test in this category
