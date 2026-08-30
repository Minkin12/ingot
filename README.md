# Ingot

## Why

I wanted a training app built around exactly how I actually program my own lifting — plan-driven, percentage-based, no guesswork on load each session — and I wanted to use the project to get real, hands-on practice with the kind of event-driven distributed systems I work with professionally: durable messaging, idempotent ingestion, independently rebuildable read models. App is functional but a work in progress

## What it does

- Runs a structured strength program (or several) with prescribed loads computed automatically from your current training maxes.
- Logs every set locally, works fully offline, and syncs in the background whenever connectivity is available.
- Detects when a heavy set suggests a new personal max and lets you confirm the update.
- Recognizes designated test days and updates your official training max from the result, feeding future sessions' prescribed loads.
- Tracks completed workouts, personal records, and training volume on a home server, independent of any single device.
- Supports running the same program through multiple cycles, carrying forward an updated training max each time.

## Design

**Engine** — a plain Java module that turns a program template plus your current maxes into a concrete session: exact weights, sets, and reps. No Android or network dependencies, so it runs identically on the phone and the server.

**App** — an Android client (MVVM, Room) that owns the actual workout, stores everything locally first, and queues changes to sync later. Nothing the user does ever waits on a network.

**Backend** — a Spring Boot service backed by an append-only event log (Postgres) and a message broker (NATS). Every meaningful action — a set logged, a workout finished, a max updated — is an event. Independent consumers read that event stream and build their own focused views: history, records, volume, progression. Any of those views can be deleted and rebuilt from the event log alone.

Data flows one direction, always: an action happens on the phone → it's recorded as an event → it syncs to the server → the server durably stores and broadcasts it → whichever services care about it react, independently of each other.