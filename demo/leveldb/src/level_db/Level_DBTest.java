package level_db;
import org.iq80.leveldb.*;
import static org.iq80.leveldb.impl.Iq80DBFactory.*;
import java.io.*;

public class Level_DBTest {

	public static void main(String[] args) {
		Options options = new Options();
		options.createIfMissing(true);
		DB db;
		try {
			db = factory.open(new File("example"), options);
			db.put(bytes("I am key"), bytes("I am value"));
			db.put(bytes("1"), bytes("I am value"));
			String value = asString(db.get(bytes("I am key")));

			System.out.println(value);
			db.delete(bytes("I am key"));
			value = asString(db.get(bytes("I am key")));
			System.out.println(value);

			db.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}