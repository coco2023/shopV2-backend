# Umi-shop-backend
1. PayPal: https://developer.paypal.com/braintree/docs/guides/ec-braintree-sdk/server-side/java
2. https://developer.paypal.com/docs/api/partner-referrals/v2/
3. https://chat.openai.com/share/1d431841-65cc-4937-98d3-2540b637d1ff [Payment-RollBack realted]

# create Reconcile Log_ERROR_DB
![Reconcile Log_ERROR_DB](doc/img/reconcile_ERROR_Log_database.png)

# create payment Log_ERROR_DB
create payment Log_ERROR_DB to save every error/interrupts occur during payment process.

![payment Log_ERROR_DB.png](doc/img/payment_ERROR_Log_database.png)

# Redis
有几种优化策略可以提高电商网站加载商品主页的效率，并减少对数据库的重复访问。以下是一些推荐的做法：

1. **缓存（Caching）**:
   - **应用层缓存**：可以在应用层面引入缓存机制，比如使用Redis或Memcached来存储热门商品的信息。这样，用户在访问商品主页时，系统首先从缓存中检索数据，如果未命中，则读取数据库并更新缓存。这将大大减少数据库的访问次数。
   - **CDN缓存**：使用内容分发网络（CDN）缓存静态资源，如商品图片、CSS和JavaScript文件，可以加快全球用户的加载速度。

2. **数据库优化**:
   - **查询优化**：确保数据库查询是高效的，比如通过合理的索引、避免全表扫描等。
   - **读写分离**：将数据库的读操作和写操作分离，使用主从复制技术，可以提高数据库的并发处理能力。

3. **按需加载/懒加载（Lazy Loading）**:
   - 对于商品列表，可以实现按需加载，即仅当用户滚动查看更多商品时才加载更多内容，而不是一次性加载所有商品。

4. **前端优化**:
   - 通过减少HTTP请求、压缩文件、合理使用HTTP缓存控制策略等方式来优化前端性能。
   - 实现异步加载（AJAX）来更新页面内容，无需重新加载整个页面。

5. **使用负载均衡**:
   - 如果网站流量较大，可以考虑使用负载均衡器分散请求到多个服务器，从而提高整体的处理能力和可用性。

6. **微服务架构**:
   - 如果您的应用非常庞大且复杂，可以考虑将其拆分为多个微服务，每个服务负责处理特定的功能。这样可以提高每个服务的专注度和效率。

通过综合应用上述策略，可以显著提高您电商网站的性能和用户体验。每种方法都有其适用场景，建议根据您的具体需求和资源情况进行选择和调整。如果需要更详细的实现建议或帮助，请随时提问！

# Customer, Supplier 在加了身份验证 改了代码以后没有办法登录（用户名密码都没有问题）
因为password Encoder在JWTProvide里面需要处理各种用户密码登录操作的password；如果把Password Encoder和其他函数放在了JWTFilter或其他地方就会报错

# 使用RabbitMQ处理（可能的）高并发订单时问题
1. 异步处理response有LocalDateTime序列化的问题
maven, 使对象class序列化
```md
确保当Jackson2JsonMessageConverter处理消息时，它将能够正确地序列化和反序列化LocalDateTime字段。

		<dependency>
			<groupId>com.fasterxml.jackson.datatype</groupId>
			<artifactId>jackson-datatype-jsr310</artifactId>
			<version>2.13.3</version> <!-- 请使用与您的Jackson核心库相匹配的版本 -->
		</dependency>
		<dependency>
			<groupId>com.fasterxml.jackson.core</groupId>
			<artifactId>jackson-core</artifactId>
			<version>2.13.3</version>
		</dependency>
```
并对`RabbitConfig`进行配置
```md 
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            builder.modules(new JavaTimeModule());
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        };
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // 注册JavaTimeModule
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // 禁用日期时间戳格式
        return objectMapper;
    }
```

2. 异步处理createPaymentAtMQ 和真正的createPayment和前端交互的问题
```md

1. **前端轮询或 WebSocket**：在前端实现一个轮询机制，定期向后端查询支付创建的状态和重定向 URL。一旦后端确认支付已创建并有了重定向 URL，前端就可以使用 JavaScript 实现页面跳转。或者，使用 WebSocket 在服务端和客户端之间建立一个双向通信通道，当支付创建完成并获取到重定向 URL 后，通过 WebSocket 直接将 URL 发送到客户端，然后客户端执行重定向。

2. **同步处理支付创建**：如果可能，考虑在用户的原始请求-响应周期内同步处理支付创建。这样，一旦创建了支付并获取了重定向 URL，就可以直接在响应中返回重定向指令。这种方法可能会增加请求的响应时间，但可以直接实现重定向。

3. **中间页面或状态页面**：引导用户到一个中间页面或状态页面，在这个页面上使用 JavaScript 定时检查支付状态。一旦检测到支付已创建并获取到重定向 URL，就使用 JavaScript 在客户端执行重定向。

4. **客户端发起支付请求**：改变流程，让客户端（比如浏览器）直接发起支付创建请求，而不是通过服务器端的异步处理。这样，服务器端可以在处理该请求时同步返回重定向 URL，从而实现重定向。

```

# add TransactionTemplate to payment process for payment rollback
 If you are facing difficulties using `TransactionSynchronizationManager` and it's not resolving the `getCurrentTransactionStatus` method, here's an alternative approach to handle transaction rollback programmatically:

1. **Using `TransactionStatus` Parameter**:

   You can modify your method signature to include a `TransactionStatus` parameter, which allows you to control the transaction programmatically. Here's an updated version of your `createPayment` method:

   ```java
   import org.springframework.transaction.annotation.Transactional;
   import org.springframework.transaction.annotation.Isolation;
   import org.springframework.transaction.TransactionStatus;
   import org.springframework.transaction.support.TransactionCallbackWithoutResult;
   import org.springframework.transaction.support.TransactionTemplate;
   import org.springframework.beans.factory.annotation.Autowired;

   @Service
   public class PayPalPaymentServiceImpl implements PayPalPaymentService {

       @Autowired
       private TransactionTemplate transactionTemplate; // Inject the TransactionTemplate

       @Override
       @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
       public PayPalPaymentResponse createPayment(SalesOrder salesOrder, TransactionStatus status) {
           try {
               Payment createdPayment = payment.create(getAPIContext());
               log.info("createPayment: " + createdPayment);

               // Find the approval URL
               String approvalUrl = createdPayment.getLinks().stream()
                       .filter(link -> "approval_url".equalsIgnoreCase(link.getRel()))
                       .findFirst()
                       .map(link -> link.getHref())
                       .orElse(null);
               log.info("approval_url: " + approvalUrl);

               PayPalPaymentResponse response = new PayPalPaymentResponse("success create payment!", createdPayment.getId(), approvalUrl);
               log.info("create response: " + response);

               return response;
           } catch (PayPalRESTException e) {
               e.printStackTrace();
               // Handle PayPal API exceptions
               log.error("Error creating payment: " + e.getMessage(), e);

               // Mark the transaction for rollback programmatically
               status.setRollbackOnly();

               return new PayPalPaymentResponse("Failed to create payment", null, null);
           } catch (Exception ex) {
               // Handle other unexpected exceptions
               log.error("Unexpected error creating payment: " + ex.getMessage(), ex);

               // Mark the transaction for rollback programmatically
               status.setRollbackOnly();

               // You can also rollback the payment or take other appropriate actions here
               return new PayPalPaymentResponse("Unexpected error", null, null);
           }
       }
   }
   ```

   In this updated code, we added a `TransactionStatus` parameter named `status` to the `createPayment` method. We use `status.setRollbackOnly()` to programmatically mark the transaction for rollback when an exception occurs.

2. **Wrap Transaction with `TransactionTemplate`**:

   Another approach is to wrap your transactional code with a `TransactionTemplate`. Here's how you can do it:

   ```java
   import org.springframework.stereotype.Service;
   import org.springframework.transaction.annotation.Transactional;
   import org.springframework.transaction.annotation.Isolation;
   import org.springframework.transaction.support.TransactionTemplate;
   import org.springframework.beans.factory.annotation.Autowired;

   @Service
   public class PayPalPaymentServiceImpl implements PayPalPaymentService {

       @Autowired
       private TransactionTemplate transactionTemplate; // Inject the TransactionTemplate

       @Override
       public PayPalPaymentResponse createPayment(SalesOrder salesOrder) {
           // Use TransactionTemplate to manage the transaction
           return transactionTemplate.execute(status -> {
               try {
                   Payment createdPayment = payment.create(getAPIContext());
                   log.info("createPayment: " + createdPayment);

                   // Find the approval URL
                   String approvalUrl = createdPayment.getLinks().stream()
                           .filter(link -> "approval_url".equalsIgnoreCase(link.getRel()))
                           .findFirst()
                           .map(link -> link.getHref())
                           .orElse(null);
                   log.info("approval_url: " + approvalUrl);

                   PayPalPaymentResponse response = new PayPalPaymentResponse("success create payment!", createdPayment.getId(), approvalUrl);
                   log.info("create response: " + response);

                   return response;
               } catch (PayPalRESTException e) {
                   e.printStackTrace();
                   // Handle PayPal API exceptions
                   log.error("Error creating payment: " + e.getMessage(), e);

                   // Mark the transaction for rollback programmatically
                   status.setRollbackOnly();

                   return new PayPalPaymentResponse("Failed to create payment", null, null);
               } catch (Exception ex) {
                   // Handle other unexpected exceptions
                   log.error("Unexpected error creating payment: " + ex.getMessage(), ex);

                   // Mark the transaction for rollback programmatically
                   status.setRollbackOnly();

                   // You can also rollback the payment or take other appropriate actions here
                   return new PayPalPaymentResponse("Unexpected error", null, null);
               }
           });
       }
   }
   ```

   In this approach, we use the `transactionTemplate.execute` method to manage the transaction and ensure proper rollback handling. This may be a cleaner way to handle transactions in your Spring application.

Please choose the approach that best suits your application's architecture and requirements. Ensure that you have the necessary Spring configuration in place for transaction management to work correctly.


# CompareTo()
The error message "Operator '<' cannot be applied to 'java.math.BigDecimal', 'double'" occurs because you are trying to compare a `BigDecimal` with a primitive `double` using the less-than (`<`) operator. In Java, you cannot directly use the `<` operator to compare a `BigDecimal` and a `double` because they are of different data types.

To compare a `BigDecimal` with a `double`, you should convert the `BigDecimal` to a `double` or the `double` to a `BigDecimal` before performing the comparison. Here's how you can do it:

1. Convert `BigDecimal` to `double`:
```
   BigDecimal bigDecimalValue = /* your BigDecimal value */;
   double doubleValue = bigDecimalValue.doubleValue();
   
   if (doubleValue < someDoubleValue) {
       // Perform your logic
   }
```

2. Convert `double` to `BigDecimal`:
```
   BigDecimal bigDecimalValue = BigDecimal.valueOf(someDoubleValue);
   BigDecimal otherBigDecimalValue = /* another BigDecimal value */;
   
   if (bigDecimalValue.compareTo(otherBigDecimalValue) < 0) {
       // Perform your logic
   }
```

Choose the appropriate approach based on your specific use case. The first approach is suitable if you want to compare a `BigDecimal` with a `double`, while the second approach is suitable if you want to compare two `BigDecimal` values.

## compareTo()
you cannot directly use the `<` operator to compare two `BigDecimal` objects because `BigDecimal` is an arbitrary-precision decimal data type, and direct comparison with `<` or `>` is not supported.

To compare two `BigDecimal` objects, you should use the `compareTo` method. Here's how you can do it:

```
BigDecimal value1 = /* your first BigDecimal value */;
BigDecimal value2 = /* your second BigDecimal value */;

int comparisonResult = value1.compareTo(value2);

if (comparisonResult < 0) {
    // value1 is less than value2
    // Perform your logic here
} else if (comparisonResult > 0) {
    // value1 is greater than value2
    // Perform your logic here
} else {
    // value1 is equal to value2
    // Perform your logic here
}
```

In the code above:

- `compareTo` returns a negative value if `value1` is less than `value2`, a positive value if `value1` is greater than `value2`, and zero if they are equal.

- Based on the result of the `compareTo` method, you can perform the appropriate logic to handle the comparison between the two `BigDecimal` objects.

# Customer Exist Payment 1
To handle the situation where the user closes the webpage and exits the payment process without completing it, you can introduce a mechanism to detect such exits and notify your application. Here's how you can do it:

1. **Frontend Detection**: You can use JavaScript on your frontend to detect when the user closes the browser window or navigates away from the page. When such an event occurs, you can send an HTTP request to your backend to notify it of the exit.

2. **Backend Handling**: On the backend, you can receive the notification and log the event, which will allow developers to be informed of the exit. You can also add additional logic, such as sending an email notification to relevant parties.

Here's an example of how you can implement this:

On the Frontend (JavaScript):
```javascript
// JavaScript code to detect page unload or browser close event
window.addEventListener('beforeunload', function (e) {
    // Send an HTTP request to your backend to notify of the exit
    fetch('/notify-exit', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ exitReason: 'User closed the browser' }),
    });
});
```

On the Backend (Spring Boot Controller):
```java
@RestController
public class PayPalPaymentController {

    @PostMapping("/notify-exit")
    public ResponseEntity<String> notifyExit(@RequestBody ExitNotification exitNotification) {
        // Log the exit event with a timestamp
        String exitReason = exitNotification.getExitReason();
        LocalDateTime exitTime = LocalDateTime.now();
        log.info("User exited the payment process. Reason: {}. Timestamp: {}", exitReason, exitTime);

        // You can add further actions here, such as sending email notifications to developers

        return ResponseEntity.ok("Exit notification received.");
    }
}

// ExitNotification class for deserializing the JSON request body
public class ExitNotification {
    private String exitReason;

    public String getExitReason() {
        return exitReason;
    }

    public void setExitReason(String exitReason) {
        this.exitReason = exitReason;
    }
}
```

In this example:

- The frontend JavaScript code listens for the `beforeunload` event, which is triggered when the user closes the browser or navigates away from the page.
- When the event is detected, it sends an HTTP POST request to the `/notify-exit` endpoint on the backend.
- The Spring Boot controller handles the `/notify-exit` endpoint, logs the exit event, and performs any additional actions you require.

This way, you can track and log instances where users exit the payment process prematurely, and you can customize the handling of such events according to your application's needs, such as notifying developers or taking other appropriate actions.

Certainly! The `customerExitsDuringPayment()` method is a placeholder for a condition that you would need to implement to detect if the customer exits the payment process. This condition should be based on your application's specific requirements and logic. It would typically involve checking some user interaction or session state to determine if the user exited during payment.

Here's an example of how you might implement such a method:

```java
@Service
public class PayPalPaymentServiceImpl implements PayPalPaymentService {

    // Other methods...

    // Placeholder for checking if the customer exits during payment
    private boolean customerExitsDuringPayment() {
        // You need to implement this method based on your application's needs.
        // It could involve checking session state, user interactions, or other factors.
        // For demonstration purposes, we'll assume a simple timeout condition.
        
        // Check if a session attribute indicating payment in progress is not present.
        // You might store such an attribute when the payment process begins.
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("paymentInProgress") == null) {
            return true; // Customer exited during payment
        }

        return false; // Customer did not exit during payment
    }
}
```

In this example:

- We assume that you have some session management in place where you set a session attribute (`"paymentInProgress"`) when the payment process begins.

- The `customerExitsDuringPayment()` method checks if this session attribute is not present. If it's not present, it assumes that the customer exited during payment and returns `true`. Otherwise, it returns `false`.

You should customize the `customerExitsDuringPayment()` method to match your application's actual logic for detecting when a customer exits the payment process. It might involve checking user interactions, session state, or any other relevant conditions.

Once you have implemented the appropriate logic for your application, you can use the `customerExitsDuringPayment()` method within your `processPayment` method, as shown in the previous response, to throw a custom exception and trigger a rollback when the customer exits during payment.

# Customer Exist Payment 2 - Rollback - a4
I apologize for any confusion. Let me provide you with a more complete example of how to integrate the frontend (ReactJS) with the backend (Spring Boot) to handle user exits and simulate interruptions in the payment process using a custom exception that triggers a rollback.

Here's the code for the backend (Spring Boot):

1. **Spring Boot Controller** (`PayPalPaymentController.java`):

```java
@RestController
public class PayPalPaymentController {

    @Autowired
    private PayPalPaymentService paymentService;

    @PostMapping("/notify-exit")
    public ResponseEntity<String> notifyExit(@RequestBody ExitNotification exitNotification) {
        // Log the exit event with a timestamp
        String exitReason = exitNotification.getExitReason();
        LocalDateTime exitTime = LocalDateTime.now();
        log.info("User exited the payment process. Reason: {}. Timestamp: {}", exitReason, exitTime);

        // Throw a custom PaymentProcessingException to simulate interruption due to exit
        throw new PaymentProcessingException("Payment creation interrupted because the customer exited.");
    }
}
```

2. **Custom Exception** (`PaymentProcessingException.java`):

```java
public class PaymentProcessingException extends RuntimeException {
    public PaymentProcessingException(String message) {
        super(message);
    }
}
```

3. **Service Interface** (`PayPalPaymentService.java`):

```java
public interface PayPalPaymentService {
    PayPalPaymentResponse createPayment(SalesOrder salesOrder);
    PaymentResponse completePayment(String paymentId, String payerId);
}
```

4. **Service Implementation** (`PayPalPaymentServiceImpl.java`):

```java
@Service
public class PayPalPaymentServiceImpl implements PayPalPaymentService {

    @Override
    @Transactional(rollbackFor = PaymentProcessingException.class, isolation = Isolation.READ_COMMITTED)
    public PayPalPaymentResponse createPayment(SalesOrder salesOrder) {
        try {
            // Payment creation logic

            return response;
        } catch (PayPalRESTException e) {
            // Handle PayPal API exceptions
            // ...
            throw new PaymentProcessingException("Payment creation interrupted due to an error.", e);
        } catch (Exception ex) {
            // Handle other unexpected exceptions
            // ...
            throw new PaymentProcessingException("Payment creation interrupted due to an unexpected error.", ex);
        }
    }

    @Override
    @Transactional(rollbackFor = PaymentProcessingException.class, isolation = Isolation.READ_COMMITTED)
    public PaymentResponse completePayment(String paymentId, String payerId) {
        try {
            // Payment execution logic

            return response;
        } catch (PayPalRESTException e) {
            // Handle PayPal API exceptions
            // ...
            throw new PaymentProcessingException("Payment execution interrupted due to an error.", e);
        } catch (Exception ex) {
            // Handle other unexpected exceptions
            // ...
            throw a PaymentProcessingException("Payment execution interrupted due to an unexpected error.", ex);
        }
    }
}
```

Now, for the frontend (ReactJS):

1. **Frontend JavaScript (ReactJS)**: You can use the `window.addEventListener` approach as previously mentioned to detect when the user closes the browser or navigates away from the page. When this event occurs, send an HTTP request to your backend to notify it of the exit.

Here's a simplified example of how you might do this in a React component:

```javascript
import React, { useEffect } from 'react';

function PayPalPaymentComponent() {
    useEffect(() => {
        window.addEventListener('beforeunload', function (e) {
            // Send an HTTP request to notify the backend of the exit
            fetch('/notify-exit', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ exitReason: 'User closed the browser' }),
            });
        });
    }, []);

    return (
        // Your PayPal payment component UI
    );
}

export default PayPalPaymentComponent;
```

Make sure you adjust the URL (`'/notify-exit'`) in the `fetch` request to match your backend endpoint.

With this setup, when the user closes the browser or navigates away from the page, the frontend will send an HTTP request to notify the backend (`/notify-exit` endpoint). The backend will then log the exit event and throw a custom `PaymentProcessingException`, which will trigger a rollback if it occurs within a transactional method.

Please note that this is a simplified example, and you may need to customize it further based on your specific ReactJS application structure and requirements.

# Customer Exist Payment 2 - Rollback a2
I apologize for any confusion. To achieve the behavior you mentioned, you need to make sure that the `/notify-exit` endpoint throws a `PaymentProcessingException` and that the `/createPayment` and `/completePayment` methods in `PayPalPaymentServiceImpl` catch this exception to trigger a rollback. Here's the complete code for this scenario:

1. Frontend (JavaScript):

```javascript
// JavaScript code to detect page unload or browser close event
window.addEventListener('beforeunload', function (e) {
    // Send an HTTP request to your backend to notify of the exit
    fetch('/notify-exit', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ exitReason: 'User closed the browser' }),
    });
});
```

2. Backend (Spring Boot Controller):

```java
@RestController
public class PayPalPaymentController {

    @Autowired
    private PayPalPaymentService paymentService;

    @PostMapping("/notify-exit")
    public ResponseEntity<String> notifyExit(@RequestBody ExitNotification exitNotification) {
        // Log the exit event with a timestamp
        String exitReason = exitNotification.getExitReason();
        LocalDateTime exitTime = LocalDateTime.now();
        log.info("User exited the payment process. Reason: {}. Timestamp: {}", exitReason, exitTime);

        // Throw a PaymentProcessingException to simulate interruption due to exit
        throw new PaymentProcessingException("Payment creation or execution interrupted because the customer exited.");
    }
}
```

3. `PayPalPaymentServiceImpl` (Backend):

```java
@Service
public class PayPalPaymentServiceImpl implements PayPalPaymentService {

    @Override
    @Transactional(rollbackFor = PaymentProcessingException.class, isolation = Isolation.READ_COMMITTED)
    public PayPalPaymentResponse createPayment(SalesOrder salesOrder) {
        try {
            // Payment creation logic

            return response;
        } catch (PayPalRESTException e) {
            // Handle PayPal API exceptions
            // ...
            throw new PaymentProcessingException("Payment creation interrupted due to an error.", e);
        } catch (Exception ex) {
            // Handle other unexpected exceptions
            // ...
            throw new PaymentProcessingException("Payment creation interrupted due to an unexpected error.", ex);
        }
    }

    @Override
    @Transactional(rollbackFor = PaymentProcessingException.class, isolation = Isolation.READ_COMMITTED)
    public PaymentResponse completePayment(String paymentId, String payerId) {
        try {
            // Payment execution logic

            return response;
        } catch (PayPalRESTException e) {
            // Handle PayPal API exceptions
            // ...
            throw new PaymentProcessingException("Payment execution interrupted due to an error.", e);
        } catch (Exception ex) {
            // Handle other unexpected exceptions
            // ...
            throw new PaymentProcessingException("Payment execution interrupted due to an unexpected error.", ex);
        }
    }
}
```

In this updated code:

- The `/notify-exit` endpoint in the `PayPalPaymentController` logs the exit event and throws a `PaymentProcessingException` to simulate an interruption due to the user's exit.

- The `createPayment` and `completePayment` methods in `PayPalPaymentServiceImpl` are annotated with `@Transactional(rollbackFor = PaymentProcessingException.class)`, which means that if a `PaymentProcessingException` is thrown within these methods (due to a premature exit or any other exception), it will trigger a rollback of the ongoing transaction.

This code ensures that if a user exits the payment process prematurely, a `PaymentProcessingException` is thrown and caught within the transactional methods, triggering a rollback of the transaction to maintain data consistency.

# Payment Error Handling
Handling errors during the payment process in a robust and user-friendly way is crucial in the industry. Your current approach using try-catch blocks and logging the errors is a good start. However, there are additional strategies and best practices you can implement to enhance error handling:

### 1. Detailed Error Responses

Instead of returning generic error messages, provide more detailed information about the error (while ensuring you don't expose sensitive data). This can help in diagnosing issues quickly.

```
return new PaymentResponse("Failed to execute payment: " + e.getDetails(), null);
```

### 2. Error Logging

Ensure that all exceptions are logged with sufficient detail. This includes the stack trace, error message, and any relevant context. It looks like you're already doing this, which is great.

### 3. Transaction Management

Your use of `TransactionSynchronizationManager` to roll back transactions in case of an error is a good practice. This ensures data integrity and consistency.

### 4. User-Friendly Messages

For the end-user, provide messages that are informative but not technical. Avoid exposing system details or stack traces to the user.

### 5. Error Categorization

Categorize errors based on their type and severity. This can help in deciding the right course of action for each error type (e.g., retrying the transaction, alerting the user, contacting support).

### 6. Alerting and Monitoring

Implement a system to alert your team of critical errors in real-time. Monitoring tools can help track error rates and identify patterns that might indicate larger issues.

### 7. Retry Mechanisms

For transient errors (like network issues), implement a retry mechanism with exponential backoff and jitter.

### 8. Fallback Strategies

Have fallback strategies for when a critical part of the payment process fails. This might include queuing transactions for later processing or switching to a backup service if available.

### 9. User Communication

Communicate with the user appropriately in case of errors. For instance, if a payment fails, inform the user clearly and suggest possible next steps.

### 10. Error Analysis

Regularly analyze the errors that occur to identify and address underlying systemic issues.

### 11. Documentation and Support

Maintain clear documentation for error codes and their meanings. Provide easy access to customer support for unresolved issues.

### Example Enhanced Error Handling:

```
} catch (PaymentProcessingException e) {
    handlePaymentError(e, "Error creating payment");
    return new PaymentResponse("Failed to create payment due to processing error", null);
} catch (PayPalRESTException e) {
    handlePaymentError(e, "Error executing PayPal payment");
    return new PaymentResponse("Failed to execute payment with PayPal", null);
} catch (Exception ex) {
    handlePaymentError(ex, "Unexpected error during payment execution");
    return new PaymentResponse("Unexpected error occurred", null);
}

private void handlePaymentError(Exception e, String logMessage) {
    log.error(logMessage + ": " + e.getMessage(), e);
    if (TransactionSynchronizationManager.isActualTransactionActive()) {
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    }
    // Additional error handling like alerting, monitoring, etc.
}
```

In this enhanced version, there's a dedicated method `handlePaymentError` for common error handling tasks, which improves code reusability and organization.


# Git
If you need to update your local repository to exactly match the remote repository, effectively accepting all changes from the remote repository and discarding any conflicting local changes, you can do so with a hard reset. This will forcefully sync your local branch to the state of the remote branch, but be aware that this will overwrite your local changes. Here's how to do it:

1. **Fetch the Latest Changes from Remote:**
   First, fetch the updates from the remote repository to make sure you have the latest state.
   ```bash
   git fetch origin
   ```
   Replace `origin` with the name of your remote if it's different.

2. **Hard Reset to Remote Branch:**
   Next, perform a hard reset to the specific branch on the remote repository. This will synchronize your local branch with the remote branch, discarding any conflicting local changes.
   ```bash
   git reset --hard origin/<branch-name>
   ```
   Replace `<branch-name>` with the name of the branch you want to sync with (e.g., `master` or `main`).

3. **Verify the Sync:**
   After the reset, your local branch should be in the exact state as the remote branch. You can verify this with:
   ```bash
   git status
   ```

4. **Clean Up Untracked Files (Optional):**
   If there are any untracked files or directories in your local workspace that are not in the repository, you might want to clean them up to match the remote repository exactly.
   ```bash
   git clean -fd
   ```
   The `-f` flag is for "force", and `-d` will remove untracked directories as well as files.

### Important Considerations

- **Data Loss:** The `git reset --hard` and `git clean` commands can lead to irreversible loss of your local changes. Make sure you really want to discard these changes before proceeding.

- **Commits:** If you have local commits that have not been pushed to the remote repository, these will be lost. If you want to preserve them, consider creating a backup branch before doing the reset:

  ```bash
  git branch backup-branch-name
  ```

- **Stash:** If you prefer not to lose your local changes entirely, you could stash them instead of discarding:

  ```bash
  git stash
  ```

  You can then later try to reapply the stashed changes with `git stash pop`, but be aware that you might encounter merge conflicts when reapplying them.

Proceed with caution when using these commands to ensure you do not unintentionally lose important work.

# ScheduledTask
Calling a controller method directly from within a scheduled task is generally not recommended. The better approach is to refactor the shared logic into a service layer method that can be called both from the controller for API requests and from the scheduled task for automated processing. This is the approach used in the example for the monthly report generation.

Here's why this approach is preferred:

1. **Separation of Concerns**: Controllers in Spring Boot are typically designed to handle HTTP requests and responses. They are part of the web layer. Scheduled tasks, however, are part of the service layer and should ideally not depend on web layer components.

2. **Reusability and Maintainability**: By placing the shared logic in a service, you can easily reuse it in different contexts (like web requests and scheduled tasks) without duplication. If the business logic changes, you only need to update it in one place.

3. **Testability**: It's easier to write unit tests for service methods than for controllers, especially when the logic doesn't inherently involve web contexts.

4. **Error Handling**: Handling errors and exceptions can be more appropriately managed within the service layer, especially for background tasks that don't interact with a user directly.

5. **Transactional Boundaries**: Services are a natural place to manage transactions, especially if your logic involves database operations that need to be handled atomically.

So, in your case, for the daily report generation task, instead of calling the controller method, you should create a service method that contains the necessary logic to generate the report. This service method can then be invoked from both the controller and the scheduled task.

Here’s an example of how you might refactor your daily report generation:

```java
@Service
public class SuppliersFinanceService {
    // ... other methods ...

    public FinancialReport generateDailyFinancialReportWrapper(Long supplierId, String daily) {
        List<LocalDateTime> dates = datesFormatConvert.convertFinancialDayFormat(daily);
        return generateDailyFinancialReport(supplierId, dates.get(0), dates.get(1), ReportType.DAILY);
    }

    // ... other methods ...
}

@RestController
@RequestMapping("/api/v1/suppliers/finance")
public class SuppliersFinanceController {

    private final SuppliersFinanceService suppliersFinanceService;

    // Constructor
    public SuppliersFinanceController(SuppliersFinanceService suppliersFinanceService) {
        this.suppliersFinanceService = suppliersFinanceService;
    }

    @GetMapping("/{supplierId}/financial-report/day")
    public ResponseEntity<?> generateDailySalesReport(@PathVariable Long supplierId, @RequestParam String daily) {
        FinancialReport financialReport = suppliersFinanceService.generateDailyFinancialReportWrapper(supplierId, daily);
        // ...
        return ResponseEntity.ok(financialReport);
    }
}
```

And similarly, in your scheduled task:

```java
@Component
public class ScheduledTasks {

    private final SuppliersFinanceService suppliersFinanceService;

    // Constructor
    public ScheduledTasks(SuppliersFinanceService suppliersFinanceService) {
        this.suppliersFinanceService = suppliersFinanceService;
    }

    @Scheduled(cron = "0 59 23 * * ?")
    public void generateDailySalesReportAutomatically() {
        // ... fetch supplier IDs and date ...

        for (String supplierId : allSupplierIds) {
            FinancialReport report = suppliersFinanceService.generateDailyFinancialReportWrapper(Long.valueOf(supplierId), currentDateStr);
            // Handle the report as necessary
        }
    }
}
```

This way, the actual logic for report generation is encapsulated within the service, and both the controller and the scheduler call this shared service method.

# Redis & Cache: 实现缓存配置以及在更新或删除产品时清除或更新缓存
为了实现缓存配置以及在更新或删除产品时清除或更新缓存，我们可以扩展您的示例代码。首先，是缓存配置类`CacheConfig`，它启用了Spring的缓存支持。接着，我们将在`ProductService`中添加逻辑来处理更新和删除产品时的缓存操作。

### 缓存配置

```java
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1)) // 设置缓存有效期一小时
            .disableCachingNullValues(); // 不缓存空值

        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(cacheConfiguration)
            .build();
    }
}
```

这个配置类定义了一个`CacheManager`的Bean，它是Spring框架中管理缓存操作的组件。这里我们使用`RedisCacheManager`，它是基于Redis的实现。我们还为缓存设置了一些默认配置，比如缓存条目的存活时间（TTL）和不缓存空值的策略。

### 更新或删除产品时清除缓存

接下来，我们需要在更新或删除产品的操作中清除相关的缓存。这里，我们使用`@CacheEvict`注解来实现这一点。我们假设`ProductService`类中有`updateProduct`和`deleteProduct`方法，我们在这些方法执行成功后清除缓存。

首先，确保`ProductService`类中有相应的方法实现：

```java
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    // 其他方法...

    @CacheEvict(value = "products", allEntries = true)
    public Product updateProduct(Long id, Product productDetails) {
        // 更新产品的逻辑
        // 假设这里有逻辑来更新产品，并返回更新后的产品
        return updatedProduct;
    }

    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long id) {
        // 删除产品的逻辑
        // 假设这里有逻辑来删除指定ID的产品
    }
}
```

然后，在控制器`ProductController`中调用这些服务方法：

```java
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    // 构造函数、注入等...

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        Product updatedProduct = productService.updateProduct(id, productDetails);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }
}
```

通过在服务层的方法上使用`@CacheEvict`注解，并设置`allEntries = true`，我们告诉Spring在这些方法成功执行后清除名为"products"的缓存中的所有条目。这确保了缓存中的数据保持最新，用户在下次请求时能获取到最新的数据。

# CDN 缓存
使用内容分发网络（CDN）缓存静态资源是一种常用的优化网站性能的方法，特别是对于全球分布的用户。CDN通过在多个地理位置分布的服务器上缓存网站的静态资源（如图片、CSS和JavaScript文件），从而使用户能够从最接近他们的位置获取这些资源，减少了加载时间和延迟。以下是实现CDN缓存的一般步骤：

### 1. 选择CDN提供商

市场上有许多CDN提供商，包括但不限于Cloudflare、Amazon CloudFront、Akamai和Google Cloud CDN。您应该根据您的需求、预算和所需功能选择合适的CDN提供商。

### 2. 配置CDN

一旦选择了CDN提供商，您需要按照他们的指南配置CDN。虽然不同的CDN提供商可能有不同的配置步骤，但大多数配置过程包括以下几个基本步骤：

- **域名配置**：您需要在CDN提供商处配置一个CDN域名（或子域名），用户通过这个域名访问您的静态资源。例如，您可以使用`cdn.example.com`作为静态资源的CDN域名。
- **源服务器设置**：指定您的原始服务器（即存储您的静态资源的服务器）地址。CDN将从这个地址拉取静态资源并缓存到其网络中。
- **缓存规则**：配置哪些资源需要被缓存，以及它们在CDN缓存中的存活时间（TTL）。您可以为不同类型的资源设置不同的缓存策略。

### 3. 更新网站以使用CDN

- **修改静态资源的引用**：将网站中静态资源的引用（如图片、CSS和JavaScript文件的URL）更改为CDN域名。例如，如果原来的图片URL是`https://example.com/images/logo.png`，使用CDN后可能变成`https://cdn.example.com/images/logo.png`。
- **确保跨域资源共享（CORS）策略适当配置**：如果您的静态资源被其他域名的页面引用，确保设置了合适的CORS策略，允许这些资源被跨域访问。

### 4. 测试和监控

在配置CDN后，您应该彻底测试网站以确保所有静态资源都能正确通过CDN加载。此外，大多数CDN提供商提供了监控和分析工具，您可以利用这些工具来监视CDN性能和流量，根据需要进行调整。

### 5. 关于图片缓存
对于图片，将它们加入缓存是提高性能的一个好方法，特别是如果这些图片被频繁访问。您可以通过各种方式来实现这一点，例如在服务器端使用类似Redis的缓存来存储图片内容，或者更常见的是，使用CDN来缓存静态资源，包括图片。确保图片URL是可缓存的，并且在CDN配置中设置了合适的缓存策略。

通过实现CDN缓存，您的网站可以显著提升全球用户的访问速度和体验。选择和配置CDN时，请参考您所选CDN提供商的具体指导和最佳实践。

# 索引
数据库优化通常涉及到几个关键方面，包括但不限于索引优化、查询优化、数据库配置调整等。在Spring Boot应用中，尽管大部分数据库相关配置和优化工作是在数据库层面进行的，但Spring Boot也提供了一些方便的方式来定义和管理数据库索引。

### 索引优化

1. **确定需要索引的字段**：
   - 分析应用中的查询模式，找出查询频繁且数据量大的表。
   - 确定哪些字段经常用于`WHERE`子句、`JOIN`条件、`ORDER BY`和`GROUP BY`子句中。这些字段是添加索引的良好候选。

2. **添加索引**：
   - **手动添加**：使用数据库管理工具或SQL语句手动在数据库中添加索引。
     ```sql
     CREATE INDEX idx_column_name ON table_name(column_name);
     ```
   - **通过Spring Boot添加**：在实体类中使用JPA注解定义索引。虽然这种方式更便于管理和版本控制，但实际的索引创建操作仍由数据库在应用启动时根据这些注解执行。例如，为`Product`实体的`productName`字段添加索引：
     ```java
     import javax.persistence.Index;
     import javax.persistence.Table;

     @Entity
     @Table(name = "product", indexes = {@Index(name = "idx_product_name", columnList = "productName")})
     public class Product {
         // 实体属性...
     }
     ```

### 注意事项

- **不要过度索引**：虽然索引可以加速查询操作，但每个额外的索引都会增加插入、更新和删除操作的成本，因为索引本身也需要维护。因此，应避免对不经常用于查询的字段添加索引。
- **索引并不总是最佳解决方案**：在某些情况下，改进查询逻辑或使用更高效的数据结构可能比添加索引更有效。
- **监控和评估**：在添加新的索引后，应该监控其对性能的影响。在某些数据库管理系统中，可以查看查询执行计划来了解索引的使用情况和效果。

### 在Spring Boot中管理索引

虽然Spring Boot允许你通过JPA注解在实体类中定义索引，但数据库层面的优化通常需要更深入的考量，包括但不限于表的物理设计、数据库的配置参数等。因此，数据库优化是一个需要结合具体数据库特性、应用的查询模式和数据访问模式来综合考虑的过程。在实践中，可能还需要利用数据库提供的专门工具和命令来完成优化工作。

# AWS S3 + CDN save
To integrate Amazon CloudFront with your `ProductImage` entities and store images on Amazon S3 (which CloudFront can then distribute), you need to modify your `saveImage` method to upload files to S3 instead of saving them locally. CloudFront will be used to deliver these images efficiently across the globe. Here's how you can do it:

### Step 1: Add AWS Java SDK Dependency

First, add the AWS Java SDK for S3 to your project dependencies. If you're using Maven, add this to your `pom.xml`:

```xml
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-java-sdk-s3</artifactId>
    <version>1.12.118</version> <!-- Use the latest version -->
</dependency>
```

### Step 2: Configure AWS Credentials and S3 Client

Configure your AWS credentials (using `~/.aws/credentials` file or environment variables) and create an S3 client instance in your service class. Ensure that the AWS user has the necessary permissions to upload files to the S3 bucket.

```java
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@Service
public class ProductImageService {

    private AmazonS3 s3Client;

    @PostConstruct
    public void init() {
        this.s3Client = AmazonS3ClientBuilder.standard()
                            .withRegion(Regions.DEFAULT_REGION) // Specify your S3 bucket region
                            .build();
    }

    // Other service methods...
}
```

### Step 3: Modify the `saveImage` Method

Adjust the `saveImage` method to upload the image to S3 and then use CloudFront URL for accessing the image.

```java
public ProductImage saveImage(Long productId, MultipartFile imageFile) {
    try {
        String bucketName = "your-s3-bucket-name";
        String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();

        // Upload file to S3
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(imageFile.getSize());
        s3Client.putObject(bucketName, fileName, imageFile.getInputStream(), metadata);

        // Construct the CloudFront URL for the uploaded image
        String cloudFrontDomainName = "your-cloudfront-distribution-domain";
        String fileUrl = "https://" + cloudFrontDomainName + "/" + fileName;

        // Save image information in the database
        ProductImage productImage = new ProductImage();
        productImage.setProductId(productId);
        productImage.setFileName(fileName);
        productImage.setFilePath(fileUrl); // Use CloudFront URL
        productImage.setFileSize(imageFile.getSize());

        return productImageRepository.save(productImage);
    } catch (IOException e) {
        throw new RuntimeException("Failed to store image file", e);
    }
}
```

### Step 4: Configure CloudFront

Create a CloudFront distribution pointing to your S3 bucket. Make sure the bucket permissions allow CloudFront to access the files. Use the CloudFront distribution domain name in your `saveImage` method to construct the URL for the uploaded files.

### Additional Notes

- Ensure that your S3 bucket is configured to host static assets and is publicly accessible, or CloudFront is configured with the proper S3 bucket access permissions.
- For production environments, consider handling AWS credentials and client configuration more securely, using IAM roles and avoiding hard-coded values.
- Monitor your AWS usage to avoid unexpected charges, especially if your application deals with large files or high traffic.

By following these steps, you'll be able to upload product images to S3 and serve them efficiently to users worldwide via CloudFront, leveraging the global CDN network for faster content delivery.

# AWS S3 + CDN + Redis Cache: getImages


# InventoryLockService: 在lockInventory中整合ReentrantLock（本地锁）和Redis分布式锁
要在`lockInventory`中整合`ReentrantLock`（本地锁）和Redis分布式锁，你可以考虑将锁的逻辑封装在一个单独的服务类中，例如`InventoryLockService`，这样可以更清晰地管理锁的逻辑，并可以在需要的地方重用这些逻辑。

### InventoryLockService 类

你可以创建一个`InventoryLockService`类来封装获取和释放本地锁以及Redis分布式锁的逻辑：

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class InventoryLockService {

    private final ConcurrentHashMap<String, ReentrantLock> localLocks = new ConcurrentHashMap<>();

    @Autowired
    private StringRedisTemplate redisTemplate;

    public ReentrantLock getLocalLock(String skuCode) {
        return localLocks.computeIfAbsent(skuCode, k -> new ReentrantLock());
    }

    public void localLock(String skuCode) {
        ReentrantLock lock = getLocalLock(skuCode);
        lock.lock();
    }

    public void localUnlock(String skuCode) {
        ReentrantLock lock = localLocks.get(skuCode);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    public boolean globalLock(String skuCode) {
        String lockKey = "lock:product:" + skuCode;
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", 10, TimeUnit.SECONDS));
    }

    public void globalUnlock(String skuCode) {
        String lockKey = "lock:product:" + skuCode;
        redisTemplate.delete(lockKey);
    }
}
```

### 在 ProductService 中使用 InventoryLockService

然后，在你的`ProductService`中注入`InventoryLockService`，并在`lockInventory`方法中使用这些锁：

```java
@Service
public class ProductService {

    @Autowired
    private InventoryLockService inventoryLockService;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public void lockInventory(String skuCode, int quantity) {
        // 尝试获取全局锁
        if (!inventoryLockService.globalLock(skuCode)) {
            throw new IllegalStateException("Unable to acquire global lock for skuCode: " + skuCode);
        }

        try {
            // 获取本地锁
            inventoryLockService.localLock(skuCode);
            try {
                Product product = productRepository.findBySkuCode(skuCode);
                if (product != null && product.getStockQuantity() >= quantity) {
                    // 锁定库存
                    product.setLockedStockQuantity(product.getLockedStockQuantity() + quantity);
                    productRepository.save(product);
                } else {
                    // 库存不足逻辑处理
                    throw new IllegalStateException("Insufficient stock for skuCode: " + skuCode);
                }
            } finally {
                // 释放本地锁
                inventoryLockService.localUnlock(skuCode);
            }
        } finally {
            // 释放全局锁
            inventoryLockService.globalUnlock(skuCode);
        }
    }
}
```

通过这种方式，你可以清晰地管理本地锁和全局锁的逻辑，同时保持`ProductService`的职责单一，专注于业务逻辑处理。`InventoryLockService`提供了锁的管理功能，使得在不同的业务场景中重用锁逻辑变得简单。

# LockManager 类: 封装本地锁和全局锁的获取和释放逻辑
为了更好地封装本地锁和全局锁的逻辑，可以创建一个专门的锁管理器类，该类能够抽象出锁的获取和释放过程，从而简化服务层代码并提高代码的可维护性和可重用性。以下是一个改进的设计方案：

### LockManager 类

创建一个`LockManager`类，其中封装了本地锁和全局锁的获取和释放逻辑。这个类可以使用模板方法模式，提供一个执行带锁操作的高阶函数，自动处理锁的获取和释放，使得业务逻辑代码更加清晰。

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

@Component
public class LockManager {

    private final ConcurrentHashMap<String, ReentrantLock> localLocks = new ConcurrentHashMap<>();

    @Autowired
    private StringRedisTemplate redisTemplate;

    private ReentrantLock getLocalLock(String key) {
        return localLocks.computeIfAbsent(key, k -> new ReentrantLock());
    }

    private boolean tryGlobalLock(String key) {
        String lockKey = "global_lock:" + key;
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", 30, TimeUnit.SECONDS));
    }

    private void releaseGlobalLock(String key) {
        String lockKey = "global_lock:" + key;
        redisTemplate.delete(lockKey);
    }

    public void executeWithLock(String key, Consumer<String> action) {
        if (!tryGlobalLock(key)) {
            throw new IllegalStateException("Unable to acquire global lock for key: " + key);
        }
        try {
            ReentrantLock lock = getLocalLock(key);
            lock.lock();
            try {
                action.accept(key);
            } finally {
                lock.unlock();
            }
        } finally {
            releaseGlobalLock(key);
        }
    }
}
```

### 使用 LockManager

在你的服务层（例如`ProductService`中），注入`LockManager`并使用`executeWithLock`方法来执行需要加锁的操作。

```java
@Service
public class ProductService {

    @Autowired
    private LockManager lockManager;

    @Autowired
    private ProductRepository productRepository;

    public void lockInventory(String skuCode, int quantity) {
        lockManager.executeWithLock(skuCode, key -> {
            Product product = productRepository.findBySkuCode(skuCode);
            if (product != null && product.getStockQuantity() >= quantity) {
                product.setLockedStockQuantity(product.getLockedStockQuantity() + quantity);
                productRepository.save(product);
            } else {
                throw new IllegalStateException("Insufficient stock for skuCode: " + skuCode);
            }
        });
    }
}
```

### 优势

- **解耦和重用**：`LockManager`将锁的逻辑从业务代码中解耦出来，使得锁的管理更加集中和统一，易于维护和重用。
- **简化业务逻辑**：通过提供一个执行带锁操作的高阶函数，`LockManager`简化了业务逻辑层的代码，使得加锁操作更加直观和易于理解。
- **灵活性**：这种设计提供了灵活性，允许在不同的业务场景中应用不同的锁策略，只需通过`LockManager`的不同实例或方法即可实现。

通过上述设计，你可以更有效地管理和使用本地锁和全局锁，同时保持业务逻辑的清晰和简洁。

# 使用依赖倒置原则和策略模式来实现锁管理
要使用依赖倒置原则和策略模式来实现锁管理，我们首先定义一个锁策略接口，然后为不同的锁策略（如Redis分布式锁和本地锁）提供实现。`LockManager`将使用这个接口来执行加锁和解锁操作，而不是直接依赖具体的锁实现。这样，锁的策略可以在不影响业务逻辑的情况下灵活更换。

### 1. 定义锁策略接口

定义一个锁策略接口，包含加锁和解锁的方法。

```java
public interface LockStrategy {
    boolean lock(String key, long timeout, TimeUnit unit);
    void unlock(String key);
}
```

### 2. 实现Redis分布式锁策略

实现`LockStrategy`接口，提供Redis分布式锁的具体实现。

```java
@Component
public class RedisLockStrategy implements LockStrategy {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean lock(String key, long timeout, TimeUnit unit) {
        String lockKey = "lock:" + key;
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", timeout, unit));
    }

    @Override
    public void unlock(String key) {
        String lockKey = "lock:" + key;
        redisTemplate.delete(lockKey);
    }
}
```

### 3. 实现本地锁策略

同样实现`LockStrategy`接口，提供本地锁的具体实现。这里使用`ReentrantLock`。

```java
@Component
public class LocalLockStrategy implements LockStrategy {

    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    @Override
    public boolean lock(String key, long timeout, TimeUnit unit) {
        ReentrantLock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
        try {
            return lock.tryLock(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public void unlock(String key) {
        ReentrantLock lock = locks.get(key);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
```

### 4. LockManager 使用策略模式

`LockManager`类使用策略模式，根据需要选择不同的锁策略。

```java
@Component
public class LockManager {

    private final LockStrategy lockStrategy;

    @Autowired
    public LockManager(LockStrategy lockStrategy) {
        this.lockStrategy = lockStrategy;
    }

    public void executeWithLock(String key, Runnable action) {
        if (lockStrategy.lock(key, 10, TimeUnit.SECONDS)) {
            try {
                action.run();
            } finally {
                lockStrategy.unlock(key);
            }
        } else {
            throw new IllegalStateException("Unable to acquire lock for key: " + key);
        }
    }
}
```

### 5. 业务逻辑中使用LockManager

在业务逻辑中注入`LockManager`，并使用`executeWithLock`方法执行需要加锁的操作。

```java
@Service
public class ProductService {

   @Autowired
   private LockManager lockManager;

   @Autowired
   private ProductRepository productRepository;

   public void lockInventory(String skuCode, int quantity) {
      lockManager.executeWithLock(skuCode, () -> {
         Product product = productRepository.findBySkuCode(skuCode)
                 .orElseThrow(() -> new ProductNotFoundException("Product not found"));
         log.info(quantity + " lockInventory product Info: " + product.getSkuCode() + " " + product.getStockQuantity());

         int availableQuantity = product.getStockQuantity() - (product.getLockedStockQuantity() != null ? product.getLockedStockQuantity() : 0);
         log.info("availableQuantity: " + availableQuantity);

         if (availableQuantity < quantity) {
            throw new InsufficientStockException("Insufficient available stock for product");
         }

         int newLockedQuantity = (product.getLockedStockQuantity() != null ? product.getLockedStockQuantity() : 0) + quantity;
         log.info("newLockedQuantity: " + newLockedQuantity + " " + product.getLockedStockQuantity() + " " + quantity);
         product.setLockedStockQuantity(newLockedQuantity);

         productRepository.save(product);
      });
   }
}
```

### 6. 配置锁策略

在Spring配置中，你可以根据需要选择合适的锁策略。例如，你可以在开发环境中使用`LocalLockStrategy`，而在生产环境中使用`RedisLockStrategy`。

通过使用依赖倒置原则和策略模式，`LockManager`的设计变得更加灵活和可扩展，你可以轻松地切换不同的锁策略，而无需修改业务逻辑代码。这也有助于提高代码的可测试性，因为你可以为测试目的提供一个简单的锁策略实现。

在Spring配置中，你可以使用Spring Profiles来为不同的环境配置不同的锁策略。例如，可以为开发环境配置`LocalLockStrategy`，而为生产环境配置`RedisLockStrategy`。

#### application-dev.properties (开发环境配置)

```properties
spring.profiles.active=dev
```

#### application-prod.properties (生产环境配置)

```properties
spring.profiles.active=prod
```

#### Spring 配置类

```java
@Configuration
public class LockStrategyConfig {

    @Bean
    @Profile("dev")
    public LockStrategy localLockStrategy() {
        return new LocalLockStrategy();
    }

    @Bean
    @Profile("prod")
    public LockStrategy redisLockStrategy(StringRedisTemplate redisTemplate) {
        return new RedisLockStrategy(redisTemplate);
    }
}
```

在这个配置类中，根据激活的Profile来创建相应的`LockStrategy` Bean。这样，在开发环境中，系统会使用`LocalLockStrategy`，而在生产环境中则会使用`RedisLockStrategy`。

### 3. 使用LockManager

在`ProductService`中注入`LockManager`，并在需要同步执行的逻辑中使用`executeWithLock`方法，如在`lockInventory`方法中所示。

通过这种方式，你可以根据不同环境的需要灵活地切换锁策略，同时保持业务逻辑的清晰和简洁。这种设计也符合单一职责原则和依赖倒置原则，提高了代码的可维护性和可扩展性。

## 要在生产环境和开发环境中同时使用本地锁和全局锁，你可以通过组合模式将两种锁策略组合起来，而不是在不同环境中选择其中一种。这样，无论是在生产还是开发环境，你的应用都会先尝试获取本地锁，然后再获取全局锁，这样可以在保持高性能的同时，确保跨多个实例的数据一致性。

### 组合锁策略

创建一个新的`CompositeLockStrategy`类，该类实现了`LockStrategy`接口，并内部维护了一个本地锁策略和一个全局锁策略。在获取锁时，它会先尝试获取本地锁，然后再尝试获取全局锁。在释放锁时，它会先释放全局锁，然后再释放本地锁。

```java
public class CompositeLockStrategy implements LockStrategy {

    private final LockStrategy localLockStrategy;
    private final LockStrategy globalLockStrategy;

    public CompositeLockStrategy(LockStrategy localLockStrategy, LockStrategy globalLockStrategy) {
        this.localLockStrategy = localLockStrategy;
        this.globalLockStrategy = globalLockStrategy;
    }

    @Override
    public boolean lock(String key, long timeout, TimeUnit unit) {
        if (localLockStrategy.lock(key, timeout, unit)) {
            if (globalLockStrategy.lock(key, timeout, unit)) {
                return true;
            } else {
                localLockStrategy.unlock(key);
            }
        }
        return false;
    }

    @Override
    public void unlock(String key) {
        globalLockStrategy.unlock(key);
        localLockStrategy.unlock(key);
    }
}
```

### 配置组合锁策略

在你的Spring配置中，配置`CompositeLockStrategy` Bean，并将本地锁策略和全局锁策略注入到它中。这样，无论在哪个环境中，都会使用这个组合锁策略。

```java
@Configuration
public class LockConfig {

    @Bean
    public LockStrategy localLockStrategy() {
        return new LocalLockStrategy();
    }

    @Bean
    public LockStrategy redisLockStrategy(StringRedisTemplate redisTemplate) {
        return new RedisLockStrategy(redisTemplate);
    }

    @Bean
    public LockStrategy compositeLockStrategy(LockStrategy localLockStrategy, LockStrategy redisLockStrategy) {
        return new CompositeLockStrategy(localLockStrategy, redisLockStrategy);
    }
}
```

### 使用组合锁策略

在`ProductService`或其他需要锁保护的服务中，注入`compositeLockStrategy`，并使用它来执行需要加锁的操作。

```java
@Service
public class ProductService {

    @Autowired
    private LockStrategy compositeLockStrategy;

    public void updateInventory(String productId, int quantity) {
        compositeLockStrategy.lock(productId, 10, TimeUnit.SECONDS);
        try {
            // 执行需要加锁的业务逻辑
        } finally {
            compositeLockStrategy.unlock(productId);
        }
    }
}
```

这样的设计既利用了本地锁的高性能特点，又通过全局锁确保了在分布式环境下的数据一致性。同时，它也遵循了设计模式原则，如单一职责原则（SRP）和开闭原则（OCP），使得系统更加灵活和可扩展。

您提供的`RabbitConfig`配置类包含了一个用于消息序列化的`Jackson2JsonMessageConverter` Bean。这是一个很好的做法，因为它能够确保消息在发送和接收时被正确地序列化和反序列化为JSON格式。但是，如果您在声明队列时遇到问题，可能还需要添加更多的配置来声明队列、交换机和绑定，以及配置消息监听器。

### 完整的RabbitMQ配置示例

以下是一个包含队列、交换机、绑定以及消息监听器配置的完整`RabbitConfig`类示例：

```java
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String QUEUE_NAME = "orderQueue";
    public static final String EXCHANGE_NAME = "orderExchange";
    public static final String ROUTING_KEY = "orderRoutingKey";

    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @RabbitListener(queues = QUEUE_NAME)
    public void listener(Object message) {
        // 处理接收到的消息
        System.out.println("Received message: " + message);
    }
}
```

这个配置类完成了以下几个关键的配置：

1. **队列**：声明了一个名为`orderQueue`的队列。
2. **交换机**：声明了一个名为`orderExchange`的直连交换机（`DirectExchange`）。
3. **绑定**：将队列和交换机通过路由键`orderRoutingKey`进行绑定。
4. **消息模板**：配置了`RabbitTemplate`，并设置了`Jackson2JsonMessageConverter`作为消息转换器。
5. **消息监听器**：通过`@RabbitListener`注解定义了一个消息监听器，用于监听`orderQueue`队列中的消息。

### 验证和调试

- 确保这个配置类被Spring扫描到。如果您的配置类不在Spring Boot的主应用类所在包或其子包中，可能需要在主应用类上使用`@ComponentScan`注解来指定额外的包扫描路径。
- 使用RabbitMQ管理界面（通常在`http://localhost:15672`）验证队列、交换机和绑定是否按预期创建。
- 检查Spring Boot应用的启动日志，查看是否有任何与RabbitMQ相关的错误信息。
- 如果仍然遇到问题，考虑增加日志级别以获取更详细的调试信息。

这样配置后，您的应用应该能够正确地与RabbitMQ进行通信，发送和接收消息。如果仍然遇到“Failed to declare queue: orderQueue”这类错误，请检查RabbitMQ服务状态、连接配置以及权限设置是否正确。

# 异步处理createPayment，生成支付订单
当使用 RabbitMQ 异步处理订单并创建 PayPal 支付时，如果你成功获取了支付的重定向 URL 但页面没有跳转到 PayPal，这通常是因为异步处理模式与页面重定向的交互方式有关。在你的场景中，`OrderMessageListener` 在接收到消息后异步调用 `payPalService.createPayment` 方法来创建支付，这个过程和用户当前的请求-响应周期是分离的。也就是说，当 `payPalService.createPayment` 执行并生成了重定向 URL 时，用户的原始 HTTP 请求可能已经结束，且响应已经发送回客户端。

在 Web 应用中，重定向通常是在 HTTP 响应中通过状态码 `302` 和 `Location` 头实现的。但在异步处理中，当你的服务获取到重定向 URL 后，无法直接修改已经完成的 HTTP 响应来实现重定向。因此，即使你获取了正确的 PayPal 重定向 URL，也无法直接引导用户的浏览器跳转到该 URL。

### 解决方案

为了解决这个问题，你可以考虑以下几种方法：

1. **前端轮询或 WebSocket**：在前端实现一个轮询机制，定期向后端查询支付创建的状态和重定向 URL。一旦后端确认支付已创建并有了重定向 URL，前端就可以使用 JavaScript 实现页面跳转。或者，使用 WebSocket 在服务端和客户端之间建立一个双向通信通道，当支付创建完成并获取到重定向 URL 后，通过 WebSocket 直接将 URL 发送到客户端，然后客户端执行重定向。

2. **同步处理支付创建**：如果可能，考虑在用户的原始请求-响应周期内同步处理支付创建。这样，一旦创建了支付并获取了重定向 URL，就可以直接在响应中返回重定向指令。这种方法可能会增加请求的响应时间，但可以直接实现重定向。

3. **中间页面或状态页面**：引导用户到一个中间页面或状态页面，在这个页面上使用 JavaScript 定时检查支付状态。一旦检测到支付已创建并获取到重定向 URL，就使用 JavaScript 在客户端执行重定向。

4. **客户端发起支付请求**：改变流程，让客户端（比如浏览器）直接发起支付创建请求，而不是通过服务器端的异步处理。这样，服务器端可以在处理该请求时同步返回重定向 URL，从而实现重定向。

每种方法都有其适用场景和权衡。根据你的具体需求和应用架构，选择最合适的解决方案。






