package com.lolson.encryptsms

import com.lolson.encryptsms.data.model.*
import com.lolson.encryptsms.utility.CryptoMagicTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    KeyTest::class,
    SecretTest::class,
    PhoneTest::class,
    SmsHelperTest::class,
    CryptoMagicTest::class,
    SmsTest::class,
    MainSharedViewModelTest::class

)
class TestUnitSuite

//Run all test in this category
