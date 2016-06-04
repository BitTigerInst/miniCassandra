# Mini-Cassandra

## Introduction
* [Cassandra](http://cassandra.apache.org) is an open sourced product invented by Facebook in 2008, and is now developed by Apache committers and contributors from many companies.
* Mini Cassandra is a simple distributed Key/Value storage system for managing large amounts of structured data spread out across many commodity servers, while providing highly available service with no single point of failure. 

## Features
- **Large Scale**
- **Fault Tolerant**
- **Availbility**    
- **Consistency**

## Basic Operation
- **Get(key)**
Get the value of specified key
- **Append(key, data)**
Append data to an existent Key/Value pair
- **Put(key, value)**
Put a Key/Value pair into database
- **Delete(key)**
Delete a Key/Value pair from database

## Requirements
1.Java >= 1.8

## Storage Impl
This part we use a [level-db](https://github.com/dain/leveldb) java version product.
In directory lib, we already provide all of its dependency library.

Serveral months later, we will provide all project's dependency to user in maven.

## Getting Started
