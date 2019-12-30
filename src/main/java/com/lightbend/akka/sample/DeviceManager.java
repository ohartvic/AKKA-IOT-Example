package com.lightbend.akka.sample;

import java.util.Set;

import akka.actor.typed.ActorRef;

public class DeviceManager {
    public interface Command {
    }

    public static final class RequestTrackDevice implements DeviceManager.Command, DeviceGroup.Command {
        public final String groupId;
        public final String deviceId;
        public final ActorRef<DeviceRegistered> replyTo;

        public RequestTrackDevice(String groupId, String deviceId, ActorRef<DeviceRegistered> replyTo) {
            this.groupId = groupId;
            this.deviceId = deviceId;
            this.replyTo = replyTo;
        }
    }

    public static final class RequestDeviceList implements DeviceManager.Command, DeviceGroup.Command {
        final long requestId;
        final String groupId;
        final ActorRef<ReplyDeviceList> replyTo;

        public RequestDeviceList(long requestId, String groupId, ActorRef<ReplyDeviceList> replyTo) {
            this.requestId = requestId;
            this.groupId = groupId;
            this.replyTo = replyTo;
        }
    }

    public static final class ReplyDeviceList {
        final long requestId;
        final Set<String> ids;

        public ReplyDeviceList(long requestId, Set<String> ids) {
            this.requestId = requestId;
            this.ids = ids;
        }
    }

    public static final class DeviceRegistered {
        public final ActorRef<Device.Command> device;

        public DeviceRegistered(ActorRef<Device.Command> device) {
            this.device = device;
        }
    }
}
