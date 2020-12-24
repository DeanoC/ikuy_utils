package ikuy_utils

import toml.Value

import java.io.{BufferedReader, BufferedWriter, FileReader, FileWriter}
import java.nio.file.{Files, Path}
import scala.util.{Failure, Success, Try}
import scala.collection.compat.immutable.LazyList

sealed trait Variant

case class ArrayV(arr: Array[Variant]) extends Variant {
	def value: Array[Variant] = arr
}

case class BigIntV(bigInt: BigInt) extends Variant {
	def value: BigInt = bigInt

	override def toString: String = {
		Try {
			bigInt.toLong
		} match {
			case Failure(exception) => s"'${bigInt.toString(16)}'"
			case Success(value)     => value.toString
		}
	}
}

case class BooleanV(boolean: Boolean) extends Variant {
	def value: Boolean = boolean
}

case class IntV(int: Int) extends Variant {
	def value: Int = int
}

case class TableV(table: Map[String, Variant]) extends Variant {
	def value: Map[String, Variant] = table
}

case class StringV(string: String) extends Variant {
	def value: String = string
}

case class DoubleV(dbl: Double) extends Variant {
	def value: Double = dbl
}

object Utils {


	def ensureDirectories(path: Path): Unit = {
		val directory = path.toFile
		if (!directory.exists()) {
			directory.mkdirs()
		}
	}

	def writeFile(path: Path, s: String): Unit = {
		val file = path.toFile
		val bw   = new BufferedWriter(new FileWriter(file))
		bw.write(s)
		bw.close()
	}

	def readFile(name: String, path: Path, klass: Class[_]): Option[String] = {
		if (!Files.exists(path.toAbsolutePath)) {
			// try resource
			Try(klass.getResourceAsStream("/" + path.toString)) match {
				case Failure(exception) =>
					println(s"$name catalog at ${name} $exception");
					None
				case Success(value)     =>
					if (value == null) {
						println(s"$name catalog at ${name} not found");
						None
					}
					Some(scala.io.Source.fromInputStream(value).mkString)
			}
		} else {
			val file = path.toAbsolutePath.toFile
			val br   = new BufferedReader(new FileReader(file))
			val s    = LazyList.continually(br.readLine())
				.takeWhile(_ != null)
				.mkString("\n")
			br.close()
			Some(s)
		}
	}

	def readToml(name: String,
	             tomlPath: Path,
	             klass: Class[_]): Map[String, Variant] = {
		println(s"Reading $name")
		val source = readFile(name, tomlPath, klass) match {
			case Some(value) => value
			case None        => println(s"${name} toml unable to be read");
				return Map[String, Variant]()
		}

		val tparsed = toml.Toml.parse(source)
		if (tparsed.isLeft) {
			println(s"${name} has failed to parse with error ${tparsed.left}");
			Map[String, Variant]()
		} else tparsed.right.get.values.map(e => (e._1 -> toVariant(e._2)))
	}

	def parseBigInt(s: String): Option[BigInt] = Try {
		val ns = s.replace("_", "")

		if (ns.startsWith("0x")) Some(BigInt(ns.substring(2), 16))
		else Some(BigInt(ns))
	} match {
		case Failure(_)     => None
		case Success(value) => value
	}

	def toString(t: Variant): String = t match {
		case str: StringV => str.value
		case b: BigIntV   => b.value.toString
		case _            => println(s"ERR $t not a string"); "ERROR"
	}

	def toArray(t: Variant): Array[Variant] = t match {
		case arr: ArrayV => arr.value
		case _           => println(s"ERR $t not an Array"); Array()
	}

	def toTable(t: Variant): Map[String, Variant] = t match {
		case tbl: TableV => tbl.value
		case _           => println(s"ERR $t not a Table"); Map[String, Variant]()
	}

	def toBoolean(t: Variant): Boolean = t match {
		case b: BooleanV => b.value
		case _           => println(s"ERR $t not a bool"); false
	}

	def toInt(t: Variant): Int = t match {
		case b: IntV    => b.value
		case b: BigIntV =>
			if (b.value < Int.MaxValue && b.value > Int.MinValue) b.value.toInt
			else {
				println(s"ERR $t not within int range");
				0
			}
		case _          => println(s"ERR $t not a int"); 0
	}

	def toBigInt(t: Variant): BigInt = t match {
		case b: BigIntV => b.value
		case _          => println(s"ERR $t not a big int"); 0
	}

	def toVariant(t: Value): Variant = t match {
		case Value.Str(v)  => parseBigInt(v) match {
			case Some(value) => BigIntV(value)
			case None        => StringV(v)
		}
		case Value.Bool(v) => BooleanV(v)
		case Value.Real(v) => DoubleV(v)
		case Value.Num(v)  => BigIntV(v)
		case Value.Tbl(v)  => TableV(v.map(e => (e._1 -> toVariant(e._2))))
		case Value.Arr(v)  => ArrayV(v.map(toVariant).toArray)
	}

	def lookupStrings(tbl: Map[String, Variant], key: String, or: String)
	: Seq[String] = if (tbl.contains(key))
		tbl(key) match {
			case ArrayV(arr)  => arr.flatMap {
				case StringV(str) => Some(str)
				case _            => None
			}.toSeq
			case StringV(str) => Seq(str)
			case _            => Seq(or)
		}
	else Seq(or)

	def lookupString(tbl: Map[String, Variant], key: String, or: String)
	: String =
		if (tbl.contains(key)) Utils.toString(tbl(key)) else or


	def lookupInt(tbl: Map[String, Variant], key: String, or: Int): Int =
		if (tbl.contains(key)) Utils.toInt(tbl(key)) else or

	def lookupBoolean(tbl: Map[String, Variant],
	                  key: String,
	                  or: Boolean): Boolean =
		if (tbl.contains(key)) Utils.toBoolean(tbl(key)) else or

	def lookupArray(tbl: Map[String, Variant], key: String): Array[Variant] =
		if (tbl.contains(key)) Utils.toArray(tbl(key)) else Array()

	def lookupBigInt(tbl: Map[String, Variant], key: String, or: BigInt)
	: BigInt = if (tbl.contains(key)) Utils.toBigInt(tbl(key)) else or

	def stringToVariant(s: String): Variant = {
		val ns = s.toLowerCase
		if (ns.contains("'")) StringV(s)
		else if (ns.contains('"')) StringV(s)
		else if (ns == "true") BooleanV(true)
		else if (ns == "false") BooleanV(false)
		else parseBigInt(ns) match {
			case Some(v) => BigIntV(v)
			case None    => Try {
				ns.toDouble
			} match {
				case Failure(_)     => StringV(s)
				case Success(value) => DoubleV(value)
			}
		}
	}
}
