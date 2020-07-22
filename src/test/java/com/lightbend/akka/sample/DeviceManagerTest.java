package com.lightbend.akka.sample;

import static org.junit.Assert.assertNotEquals;

import com.lightbend.akka.sample.DeviceManager.DeviceRegistered;
import com.lightbend.akka.sample.DeviceManager.RequestTrackDevice;

import org.junit.ClassRule;
import org.junit.Test;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;

public class DeviceManagerTest {

    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testReplyToRegistrationRequests() {
        TestProbe<DeviceRegistered> probe = testKit.createTestProbe(DeviceRegistered.class);
        ActorRef<DeviceManager.Command> managerActor = testKit.spawn(DeviceManager.create());

        managerActor.tell(new RequestTrackDevice("group1", "device", probe.getRef()));
        DeviceRegistered registered1 = probe.receiveMessage();

        // another group
        managerActor.tell(new RequestTrackDevice("group2", "device", probe.getRef()));
        DeviceRegistered registered2 = probe.receiveMessage();
        assertNotEquals(registered1.device, registered2.device);
    }
}