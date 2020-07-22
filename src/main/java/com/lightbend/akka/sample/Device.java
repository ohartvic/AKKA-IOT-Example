package com.lightbend.akka.sample;

import java.util.Optional;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Device extends AbstractBehavior<Device.Command> {

  public interface Command {
  }

  public static final class RecordTemperature implements Command {
    final long requestId;
    final double value;
    final ActorRef<TemperatureRecorded> replyTo;

    public RecordTemperature(final long requestId, final double value, final ActorRef<TemperatureRecorded> replyTo) {
      this.requestId = requestId;
      this.value = value;
      this.replyTo = replyTo;
    }
  }

  public static final class TemperatureRecorded {
    final long requestId;

    public TemperatureRecorded(final long requestId) {
      this.requestId = requestId;
    }
  }

  public static final class ReadTemperature implements Command {
    final long requestId;
    final ActorRef<RespondTemperature> replyTo;

    public ReadTemperature(final long requestId, final ActorRef<RespondTemperature> replyTo) {
      this.requestId = requestId;
      this.replyTo = replyTo;
    }
  }

  public static final class RespondTemperature {
    final long requestId;
    final Optional<Double> value;
    final String deviceId;

    public RespondTemperature(final long requestId, final String deviceId, final Optional<Double> value) {
      this.requestId = requestId;
      this.deviceId = deviceId;
      this.value = value;
    }
  }

  // #passivate-msg
  static enum Passivate implements Command {
    INSTANCE
  }
  // #passivate-msg

  public static Behavior<Command> create(final String groupId, final String deviceId) {
    return Behaviors.setup(context -> new Device(context, groupId, deviceId));
  }

  private final String groupId;
  private final String deviceId;

  private Optional<Double> lastTemperatureReading = Optional.empty();

  private Device(final ActorContext<Command> context, final String groupId, final String deviceId) {
    super(context);
    this.groupId = groupId;
    this.deviceId = deviceId;

    context.getLog().info("Device actor {}-{} started", groupId, deviceId);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(RecordTemperature.class, this::onRecordTemperature)
        .onMessage(ReadTemperature.class, this::onReadTemperature)
        .onMessage(Passivate.class, m -> Behaviors.stopped())
        .onSignal(PostStop.class, signal -> onPostStop())
        .build();
  }

  private Behavior<Command> onRecordTemperature(final RecordTemperature r) {
    getContext().getLog().info("Recorded temperature reading {} with {}", r.value, r.requestId);
    lastTemperatureReading = Optional.of(r.value);
    r.replyTo.tell(new TemperatureRecorded(r.requestId));
    return this;
  }

  private Behavior<Command> onReadTemperature(final ReadTemperature r) {
    r.replyTo.tell(new RespondTemperature(r.requestId, deviceId, lastTemperatureReading));
    return this;
  }

  private Behavior<Command> onPostStop() {
    getContext().getLog().info("Device actor {}-{} stopped", groupId, deviceId);
    return Behaviors.stopped();
  }
}