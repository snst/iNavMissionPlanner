package de.meek.inavmissionplanner;

import java.util.ArrayList;

public class MspWaypointList {

    ArrayList<Waypoint> waypoints_ = new ArrayList<>();

    public void upload() {
        MspHandler msp = App.getInstance().getMsp();

        byte n = 1;
        for (Waypoint wp: waypoints_) {

            byte flag = wp.flags_;
            if (n == waypoints_.size()) {
                flag |= Waypoint.FLAG_LAST;
            }
            byte[] msg = msp.serialize_MSP_SET_WP(n++, wp.action_, wp.lat_, wp.lon_, wp.alt_, wp.p1_, wp.p2_, wp.p3_, flag);
            App.getInstance().request(msg);
        }
    }

    public boolean createFromMavlinkMissionPlan(Mavlink.MissionPlan missionPlan) {
        waypoints_.clear();
        if (missionPlan.mission != null) {

            for (Mavlink.MissionItem m : missionPlan.mission.items
                 ) {

                Waypoint wp = new Waypoint();
                switch (m.command) {
                    case Mavlink.MissionItem.MAV_CMD_NAV_WAYPOINT:
                    case Mavlink.MissionItem.MAV_CMD_DO_CHANGE_SPEED:
                        case Mavlink.MissionItem.MAV_CMD_NAV_TAKEOFF:
                    {
                        wp.action_ = Waypoint.ACTION_WAYPOINT;
                    }
                }

                wp.alt_ = (int)(m.getAltitude() * 100.0f);
                wp.lat_ = Convert.getIntLatFromLatLng(m.getLatLng());
                wp.lon_ = Convert.getIntLonFromLatLng(m.getLatLng());
                waypoints_.add(wp);
            }
            return true;
        }
        return false;
    }

    public void add(byte nr, byte action, int lat, int lon, int alt, short p1, short p2, short p3, byte flags) {
        if (nr == 0) {
            waypoints_.clear();
        }
        Waypoint wp = new Waypoint(nr, action, lat, lon, alt, p1, p2, p3, flags);
        waypoints_.add(wp);
    }
}
