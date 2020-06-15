package com.thingverse.backend.interceptors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, BehaviorInterceptor, Signal, TypedActorContext}
import com.thingverse.backend.api.interceptors.InterceptionHandler

import scala.reflect.ClassTag

object ThingverseBehaviourInterceptor {
  def apply[T: ClassTag](handler: InterceptionHandler[T])(behavior: Behavior[T]): Behavior[T] = {
    val interceptor = new ThingverseBehaviourInterceptor[T](handler)
    Behaviors.intercept(() => interceptor)(behavior)
  }
}

final class ThingverseBehaviourInterceptor[T: ClassTag] private(handler: InterceptionHandler[T])
  extends BehaviorInterceptor[T, T] {

  import BehaviorInterceptor._

  override def aroundStart(ctx: TypedActorContext[T], target: PreStartTarget[T]): Behavior[T] = {
    handler.handleStart(ctx)
    super.aroundStart(ctx, target)
  }

  override def aroundReceive(ctx: TypedActorContext[T], msg: T, target: ReceiveTarget[T]): Behavior[T] = {
    handler.handleMessage(ctx, msg)
    val ret = target(ctx, msg)
    if (Behavior.isUnhandled(ret)) {
      ctx.asScala.log.warn(s"Intercepted unhandled message: $msg")
    }
    ret
  }

  override def aroundSignal(ctx: TypedActorContext[T], signal: Signal, target: SignalTarget[T]): Behavior[T] = {
    handler.handleSignal(ctx, signal)
    target(ctx, signal)
  }

  override def toString: String = "ThingverseBehaviourInterceptor"

  override def isSame(other: BehaviorInterceptor[Any, Any]): Boolean =
    other match {
      case _: ThingverseBehaviourInterceptor[_] => true
      case _ => super.isSame(other)
    }
}