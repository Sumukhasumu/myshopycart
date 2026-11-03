
import {
  AfterViewInit,
  Component,
  OnDestroy,
  computed,
  signal
} from '@angular/core';

type ServiceId =
  | 'auth'
  | 'product'
  | 'cart'
  | 'order'
  | 'inventory'
  | 'payment'
  | 'notification';

type InfrastructureId =
  | 'gateway'
  | 'config'
  | 'eureka'
  | 'kafka'
  | 'redis'
  | 'rabbitmq'
  | 'minio';

interface ServiceDetails {
  name: string;
  port: number;
  description: string;
  database: string;
  technologies: string;
  api: string;
  integrations: string;
}

interface InfrastructureDetails {
  name: string;
  port: number;
  description: string;
  purpose: string;
  usage: string;
}

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements AfterViewInit, OnDestroy {

  protected readonly title = signal('architecture-explorer-temp');

  protected readonly activeSection = signal('overview');

  protected readonly selectedService = signal<ServiceId | null>(null);

  protected readonly selectedInfrastructure =
    signal<InfrastructureId | null>(null);

  private readonly serviceDetails: Record<ServiceId, ServiceDetails> = {
    auth: {
      name: 'Auth Service',
      port: 8081,
      description:
        'Handles user registration, login, authentication configuration and user address management.',
      database: 'PostgreSQL · myshopycart_auth',
      technologies: 'Spring Boot · Spring Security · JWT · JPA',
      api: '/api/auth · /api/v1/addresses',
      integrations: 'API Gateway · Eureka · JWT authentication'
    },

    product: {
      name: 'Product Service',
      port: 8082,
      description:
        'Manages product information, product categories, product search and product images.',
      database: 'MongoDB · myshopycart_product',
      technologies: 'Spring Boot · Spring Data MongoDB · Redis Cache',
      api: '/api/products',
      integrations: 'API Gateway · Eureka · Redis · Auth'
    },

    cart: {
      name: 'Cart Service',
      port: 8084,
      description:
        'Manages customer shopping carts, cart items and wishlists.',
      database: 'PostgreSQL · myshopycart_cart',
      technologies: 'Spring Boot · Spring Data JPA · Hibernate',
      api: '/api/carts · /api/wishlists',
      integrations: 'Product Service · Eureka · JWT authentication'
    },

    order: {
      name: 'Order Service',
      port: 8085,
      description:
        'Coordinates order creation, checkout, payment-related workflows and invoice operations.',
      database: 'PostgreSQL · myshopycart_order',
      technologies: 'Spring Boot · Spring Data JPA · Kafka',
      api: '/api/orders',
      integrations: 'Cart · Product · Inventory · Payment · Auth · Kafka · MinIO'
    },

    inventory: {
      name: 'Inventory Service',
      port: 8083,
      description:
        'Manages product stock, stock adjustments, availability, reservation, release and commit operations.',
      database: 'PostgreSQL · myshopycart_inventory',
      technologies: 'Spring Boot · Spring Data JPA · Hibernate',
      api: '/api/inventory',
      integrations: 'Eureka · JWT authentication · Internal service token configuration'
    },

    payment: {
      name: 'Payment Service',
      port: 8086,
      description:
        'Handles payment operations and supports payment lookup using an idempotency key.',
      database: 'PostgreSQL · myshopycart_payment',
      technologies: 'Spring Boot · Spring Data JPA · Flyway · Resilience4j',
      api: '/api/payments',
      integrations: 'Eureka · Kafka producer configuration · Retry · Circuit Breaker · TimeLimiter'
    },

    notification: {
      name: 'Notification Service',
      port: 8087,
      description:
        'Supports notification status tracking and configured email and messaging integrations.',
      database: 'PostgreSQL · myshopycart_notification',
      technologies: 'Spring Boot · Spring Data JPA · Flyway',
      api: '/api/v1/notifications/status',
      integrations: 'Kafka consumer · RabbitMQ · SMTP email · MSG91 configuration · Order Service'
    }
  };

  private readonly infrastructureDetails:
    Record<InfrastructureId, InfrastructureDetails> = {

    gateway: {
      name: 'API Gateway',
      port: 8080,
      description:
        'Provides the central entry point for client requests and forwards requests to backend microservices.',
      purpose: 'API routing and application entry point',
      usage:
        'Spring Cloud Gateway · Eureka service discovery · Config Server'
    },

    config: {
      name: 'Config Server',
      port: 8888,
      description:
        'Provides centralized external configuration to services through the Spring Cloud Config Server.',
      purpose: 'Centralized application configuration',
      usage:
        'Native configuration repository containing service-specific YAML files'
    },

    eureka: {
      name: 'Eureka Discovery',
      port: 8761,
      description:
        'Maintains the service registry so registered microservices can discover other service instances.',
      purpose: 'Service registration and discovery',
      usage:
        'Spring Cloud Netflix Eureka Server and Eureka clients'
    },

    kafka: {
      name: 'Apache Kafka',
      port: 9092,
      description:
        'Provides event messaging infrastructure for asynchronous communication between configured services.',
      purpose: 'Event streaming and messaging',
      usage:
        'Order Service producer configuration · Payment Service producer configuration · Notification Service consumer configuration'
    },

    redis: {
      name: 'Redis',
      port: 6379,
      description:
        'Provides caching support for product data to reduce repeated database access.',
      purpose: 'Product caching',
      usage:
        'Product Service RedisCacheConfig · Spring Cache annotations'
    },

    rabbitmq: {
      name: 'RabbitMQ',
      port: 5672,
      description:
        'Provides message broker connectivity configured in the Notification Service.',
      purpose: 'Message broker integration',
      usage:
        'Notification Service RabbitMQ connection configuration'
    },

    minio: {
      name: 'MinIO',
      port: 9100,
      description:
        'Provides S3-compatible object storage configured for invoice-related files.',
      purpose: 'Invoice object storage',
      usage:
        'Order Service MinIO endpoint and invoice bucket configuration'
    }
  };

  protected readonly selectedServiceDetails = computed(() => {
    const serviceId = this.selectedService();

    return serviceId
      ? this.serviceDetails[serviceId]
      : null;
  });

  protected readonly selectedInfrastructureDetails = computed(() => {
    const infrastructureId = this.selectedInfrastructure();

    return infrastructureId
      ? this.infrastructureDetails[infrastructureId]
      : null;
  });

  private observer?: IntersectionObserver;

  selectService(serviceId: ServiceId): void {
    this.selectedService.set(serviceId);
    this.selectedInfrastructure.set(null);
  }

  closeServiceDetails(): void {
    this.selectedService.set(null);
  }

  selectInfrastructure(infrastructureId: InfrastructureId): void {
    this.selectedInfrastructure.set(infrastructureId);
    this.selectedService.set(null);
  }

  closeInfrastructureDetails(): void {
    this.selectedInfrastructure.set(null);
  }

  ngAfterViewInit(): void {
    if (typeof IntersectionObserver === 'undefined') {
      return;
    }

    const sections = document.querySelectorAll<HTMLElement>(
      'main section[id]'
    );

    this.observer = new IntersectionObserver(
      (entries) => {
        const visibleSections = entries
          .filter(entry => entry.isIntersecting)
          .sort(
            (a, b) =>
              a.boundingClientRect.top - b.boundingClientRect.top
          );

        if (visibleSections.length > 0) {
          this.activeSection.set(visibleSections[0].target.id);
        }

        if (window.scrollY < 200) {
          this.activeSection.set('overview');
        }
      },
      {
        root: null,
        rootMargin: '-20% 0px -65% 0px',
        threshold: 0
      }
    );

    sections.forEach(section => {
      this.observer?.observe(section);
    });
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }
}
