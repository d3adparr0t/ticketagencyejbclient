package com.alchesoft.training.jboss.client;


import com.alchesoft.training.jboss.beans.TheatreBooker;
import com.alchesoft.training.jboss.beans.TheatreInfo;
import com.alchesoft.training.jboss.exceptions.InsufficientFundsException;
import com.alchesoft.training.jboss.exceptions.SeatBookedException;
import com.alchesoft.training.jboss.utils.IOUtils;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RemoteEJBClient {

    private static Logger log = Logger.getLogger(RemoteEJBClient.class.getName());

    private static final Hashtable<String, String> jndiProperties = new Hashtable<>();

    private static final String MODULE_NAME = "ticket-agency-ejb";

    public static void main(String[] args) throws Exception {

        Logger.getLogger("org.jboss").setLevel(Level.ALL);
        Logger.getLogger("org.xnio").setLevel(Level.ALL);

        runBookingApp();
    }

    private static void runBookingApp() throws NamingException {
        jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        TheatreInfo info = lookupTheatreInfoEJB();
        TheatreBooker booker = lookupTheatreBookerEJB();
        Future<String> future = null;
        while (true) {
            String command = IOUtils.readLine("> ");
            if ("book".equals(command)) {
                bookSeat(booker);
            } else if ("bookasync".equals(command)) {
                future = bookAsync(booker);
            } else if ("mail".equals(command)) {
                receiveMail(future);
            } else if ("list".equals(command)) {
                log.info(info.printSeats());
            } else if ("quit".equals(command)) {
                log.info("Bye");
                return;
            } else {
                log.info("Unknown command " + command);
            }
        }
    }

    private static void receiveMail(Future<String> future) {
        if (future== null || !future.isDone()) {
            log.info("No mail received... be patient.");
            return;
        }
        try {
            String result = future.get();
            log.info("Mail received: " + result);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static Future<String> bookAsync(TheatreBooker booker) {
        AtomicInteger seatId = new AtomicInteger(0);
        try {
        seatId.set(IOUtils.readInt("Enter seat ID: "));
        } catch (NumberFormatException nfe) {
            log.info("Wrong seat ID format");
            return null;
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("Booking issued. Please check your mail for further details.");
        return booker.bookSeatAsync(seatId.get());
    }

    private static void bookSeat(TheatreBooker booker) {
        AtomicInteger seatId = new AtomicInteger(0);
        try {
            seatId.set(IOUtils.readInt("Enter seat ID: "));
        } catch (NumberFormatException nfe) {
            log.info("Wrong seat ID format");
            return;
        }
        try {
            String msg = booker.bookSeat(seatId.get());
            log.info(msg);
        } catch (SeatBookedException | InsufficientFundsException e) {
            log.info(e.getMessage());
        } catch (Exception e) {
            log.warning(e.getMessage());
        }
    }

    private static TheatreBooker lookupTheatreBookerEJB() throws NamingException {
        Context context = new InitialContext(jndiProperties);
        String beanName = "TheatreBookerBean";
        String viewClassName = TheatreBooker.class.getName();
        String jndiUrl = "ejb:/" +MODULE_NAME + "/" + beanName + "!" + viewClassName + "?stateful";
        log.info("Looking up: " + jndiUrl);
        return (TheatreBooker) context.lookup(jndiUrl);
    }

    private static TheatreInfo lookupTheatreInfoEJB() throws NamingException {
        Context context = new InitialContext(jndiProperties);
        String beanName = "TheatreInfoBean";
        String viewClassName = TheatreInfo.class.getName();
        String jndiUrl = "ejb:/" + MODULE_NAME + "/" + beanName + "!" + viewClassName;
        log.info("Looking up: " + jndiUrl);
        return (TheatreInfo) context.lookup(jndiUrl);
    }
}
