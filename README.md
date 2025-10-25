# Schema-Guided Reasoning in Kotlin

Kotlin implementation of the schema-guided-reasoning project using Spring AI and OpenAI.

## Overview

This project demonstrates Schema-Guided Reasoning (SGR) with OpenAI. It:
- Implements a business agent capable of planning and reasoning
- Implements tool calling using only SGR and simple dispatch
- Uses a simple (inexpensive) non-reasoning model

The agent helps run a small business - selling courses to help achieve AGI faster.

## Prerequisites

- JDK 21
- OpenAI API key

## Setup

Set your OpenAI API key as an environment variable:

```bash
export OPENAI_API_KEY=your-api-key-here
```

## Running the Test

Run the test to see the agent in action:

```bash
./gradlew test --tests SchemaGuidedReasoningTest
```

Or run with verbose output:

```bash
./gradlew test --tests SchemaGuidedReasoningTest --info
```

## How It Works

1. **In-Memory Database**: Simulates a CRM with invoices, emails, products, and rules
2. **Tool Commands**: Agent can issue invoices, send emails, create rules, void invoices, and get customer data
3. **Task Execution**: Agent executes a series of tasks sequentially, using reasoning to determine required steps
4. **NextStep Schema**: Defines the structure for AI planning and execution

## Example Tasks

The test includes 7 tasks that demonstrate:
- Creating customer rules (discounts, email preferences)
- Issuing invoices with proper calculations
- Sending emails with invoice attachments
- Voiding and reissuing invoices
- Applying complex business logic based on stored rules

## Project Structure

- `Models.kt` - Data classes for products, invoices, emails, and tool commands
- `Database.kt` - In-memory database with products and storage
- `ToolDispatcher.kt` - Dispatches tool commands to appropriate handlers
- `SchemaGuidedReasoning.kt` - Core reasoning loop and OpenAI integration
- `SchemaGuidedReasoningTest.kt` - Test class with example tasks

## Learn More

- [Schema-Guided Reasoning Guide](https://abdullin.com/schema-guided-reasoning/)
- [Demo Walkthrough](https://abdullin.com/schema-guided-reasoning/demo)
