package io.appium.multiplatform.jvm

import build.buf.protovalidate.Validator
import build.buf.protovalidate.ValidatorFactory

val pbValidator: Validator = ValidatorFactory.newBuilder().build()