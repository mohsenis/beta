package com.library.samples;

import java.util.Date;
import com.vividsolutions.jts.geom.Point;
import org.hibernate.Session;

public class event {
	private Long id;
    private String title;
    private Date date;
    private Point location;

    public Event() {
    }

    public Long getId() {
        return id;
    }

    private void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Point getLocation() {
        return this.location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }
}
