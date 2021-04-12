package com.lolson.encryptsms

import com.lolson.encryptsms.data.model.KeyTest
import com.lolson.encryptsms.data.model.PhoneTest
import com.lolson.encryptsms.data.model.SecretTest
import com.lolson.encryptsms.data.model.SmsTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    KeyTest::class,
    SecretTest::class,
    PhoneTest::class,
    SmsTest::class
//    MainSharedViewModelTest::class

)
class TestUnitSuite

//Run all test in this category
