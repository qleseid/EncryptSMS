package com.example.encryptsms

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    MainWithFabActivityTest::class,
    MainActivityWithEncryptTest::class,
    AboutActivityTest2::class

)
class TestSuite

//Run all test in this category
