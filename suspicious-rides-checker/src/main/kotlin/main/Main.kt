package main

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody

import org.apache.logging.log4j.LogManager
import java.io.File

private val logger = LogManager.getLogger()

fun main(args: Array<String>) {

    val cla = mainBody { ArgParser(args).parseInto(::Conf) }

    File("/home/arjun/tutorials/").walk().forEach {
        println(it)
    }

}