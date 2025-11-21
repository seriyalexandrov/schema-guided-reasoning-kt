# Schema-Guided Reasoning in Kotlin

Kotlin implementation of the [schema-guided-reasoning demo](https://abdullin.com/schema-guided-reasoning/demo)
by [Rinat Abdullin](https://www.linkedin.com/in/abdullin) using Spring AI and OpenAI.

## Overview

This Python code demonstrates Schema-Guided Reasoning (SGR) with OpenAI, Springboot, and Kotlin. It:
- Implements a business agent capable of planning and reasoning using Structured Output of LLM
- Implements tool calling using only SGR and simple dispatch
- Uses an affordable non-reasoning gpt-5-mini model

To give this agent something to work with, we ask it to help with running
a small business - selling courses to help to achieve AGI faster.

Once this script starts, it will emulate in-memory CRM with invoices,
emails, products, and rules. Then it will execute sequentially a set of
tasks (see TASKS below). To carry them out, Agent will have to use
tools to issue invoices, create rules, send emails, and a few others.

Read [more about SGR](http://abdullin.com/schema-guided-reasoning/)
Author of the original Python demo: [Rinat Abdullin](https://www.linkedin.com/in/abdullin)

## Prerequisites

Set your OpenAI API key as an environment variable:

```bash
export OPENAI_API_KEY=your-api-key-here
```

## Running the Test

Run the test to see the agent in action:

```bash
./gradlew test --tests SchemaGuidedReasoningTest
```

## Technical Details
OpenAI API supports structure output with decoding constraints, which means a lower probability of the output being incorrect.
[OpenAI Structured Output Python](https://platform.openai.com/docs/guides/structured-outputs/supported-schemas)
[Spring Boot configuration] (https://spring.io/blog/2024/08/09/spring-ai-embraces-openais-structured-outputs-enhancing-json-response#using-the-beanoutputconverter)
[Spring Blog article](https://spring.io/blog/2024/08/09/spring-ai-embraces-openais-structured-outputs-enhancing-json-response#using-the-beanoutputconverter)
