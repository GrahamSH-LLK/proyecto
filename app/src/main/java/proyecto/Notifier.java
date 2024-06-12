package proyecto;

import java.util.ArrayList;

public class Notifier {
    public static ArrayList<Notifiable> objects = new ArrayList<Notifiable>(); // Static arraylist to store all objects that need to be notified

    public static void emit(String type, String value) {
        for (Notifiable object : objects) {
            object.acceptEvent(type, value); 
        }
    }

    public static void register(Notifiable object) {
        objects.add(object);
    }
}
