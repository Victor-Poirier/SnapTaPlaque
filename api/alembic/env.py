"""
alembic/env.py — Alembic migration environment configuration for SnapTaPlaque.

This module is the entry point that Alembic invokes whenever a migration
command is executed (e.g., ``alembic upgrade head``, ``alembic revision
--autogenerate``). It configures the SQLAlchemy engine, loads the ORM
metadata, and decides whether to run migrations in **online** or **offline**
mode.

Online mode:
    Establishes a live database connection and executes migration operations
    directly against the target PostgreSQL instance. This is the default
    mode used during development and deployment.

Offline mode:
    Generates raw SQL statements to stdout (or a file) without connecting
    to a database. Useful for generating migration scripts that will be
    applied manually by a DBA or through a controlled release process.

Key customizations (relative to the default Alembic template):
    - The ``api/`` directory is added to ``sys.path`` so that application
      modules (``app.config``, ``app.database``) can be imported regardless
      of the working directory or virtualenv layout.
    - The SQLAlchemy connection URL is dynamically sourced from
      ``app.config.settings.DATABASE_URL`` (loaded from environment
      variables / ``.env``), overriding the static value in ``alembic.ini``.
    - All ORM models (``User``, ``Prediction``) are explicitly imported to
      ensure Alembic's ``--autogenerate`` detects every table, column, and
      constraint.
    - ``compare_type=True`` and ``compare_server_default=True`` are enabled
      so that autogenerate picks up column type changes and server default
      modifications — not just structural additions/removals.

Usage:
    This file is not meant to be executed directly. It is called by the
    Alembic CLI::

        alembic upgrade head          # Apply all pending migrations
        alembic revision --autogenerate -m "add column"  # Auto-detect changes
        alembic downgrade -1          # Roll back the last migration
"""

from logging.config import fileConfig
from sqlalchemy import engine_from_config
from sqlalchemy import pool
from alembic import context
import sys
import os
from pathlib import Path

# ------------------------------------------------------------------------------
# Python Path Configuration
# ------------------------------------------------------------------------------
# Resolve the absolute path to the ``api/`` directory (one level above the
# ``alembic/`` package) and prepend it to ``sys.path``. This guarantees that
# ``from app.xxx import ...`` statements work correctly even when Alembic is
# invoked from a different working directory (e.g., the project root or a
# CI runner).
# ------------------------------------------------------------------------------
api_dir = str(Path(__file__).resolve().parent.parent)
if api_dir not in sys.path:
    sys.path.insert(0, api_dir)

# ------------------------------------------------------------------------------
# Application Imports
# ------------------------------------------------------------------------------
# Import the application settings to obtain the database URL at runtime,
# and the declarative Base to provide Alembic with the target metadata.
# ------------------------------------------------------------------------------
from app.config import settings
from app.database import Base

# ------------------------------------------------------------------------------
# Explicit Model Imports
# ------------------------------------------------------------------------------
# All SQLAlchemy ORM models MUST be imported here so that their table
# definitions are registered on ``Base.metadata`` before Alembic inspects it.
# Without these imports, ``--autogenerate`` would see an empty metadata
# object and generate no migration operations (or even drop existing tables).
#
# When adding a new model to the application, add a corresponding import
# line here to keep autogenerate in sync.
# ------------------------------------------------------------------------------
from app.database import User, Prediction

# ------------------------------------------------------------------------------
# Alembic Config Object
# ------------------------------------------------------------------------------
# ``config`` is the Alembic ``Config`` object, which provides access to
# values defined in ``alembic.ini``. It is used below to override the
# database URL and to configure Python logging.
# ------------------------------------------------------------------------------
config = context.config

# Override the static ``sqlalchemy.url`` value in alembic.ini with the
# dynamic URL from the application's settings. This ensures that the same
# DATABASE_URL environment variable controls both the FastAPI app and
# Alembic migrations, avoiding configuration drift.
config.set_main_option("sqlalchemy.url", settings.DATABASE_URL)

# ------------------------------------------------------------------------------
# Logging Configuration
# ------------------------------------------------------------------------------
# Reads the [loggers], [handlers], and [formatters] sections from
# ``alembic.ini`` and applies them via Python's ``logging.config``.
# The guard avoids a TypeError when config_file_name is None (e.g., when
# Alembic is used programmatically without an .ini file).
# ------------------------------------------------------------------------------
if config.config_file_name is not None:
    fileConfig(config.config_file_name)

# ------------------------------------------------------------------------------
# Target Metadata
# ------------------------------------------------------------------------------
# Point Alembic's autogenerate engine at the application's declarative Base
# metadata. This is the object Alembic compares against the live database
# schema to determine what has changed.
# ------------------------------------------------------------------------------
target_metadata = Base.metadata


def run_migrations_offline() -> None:
    """Run migrations in 'offline' mode.

    In this mode Alembic does **not** create a live database connection.
    Instead, it generates the SQL statements that *would* be executed and
    writes them to stdout (or a configured output file). This is useful for:

    - Reviewing migration SQL before applying it in a controlled environment.
    - Generating SQL scripts for DBA-managed deployments where direct
      ORM access to production is not permitted.

    Configuration options:
        ``literal_binds=True``
            Renders bound parameters inline in the SQL output so that the
            generated script is self-contained and copy-pasteable.
        ``compare_type=True``
            Detects column type changes (e.g., ``String(50)`` → ``String(100)``).
        ``compare_server_default=True``
            Detects changes to server-side column defaults.
    """
    url = config.get_main_option("sqlalchemy.url")
    context.configure(
        url=url,
        target_metadata=target_metadata,
        literal_binds=True,
        dialect_opts={"paramstyle": "named"},
        compare_type=True,
        compare_server_default=True,
    )

    with context.begin_transaction():
        context.run_migrations()


def run_migrations_online() -> None:
    """Run migrations in 'online' mode.

    Establishes a live connection to the PostgreSQL database using the
    SQLAlchemy engine built from ``alembic.ini`` settings (with the URL
    overridden by ``app.config.settings.DATABASE_URL``).

    The connection uses ``pool.NullPool`` to avoid keeping idle connections
    open after the migration process completes. Each migration command gets
    a fresh connection that is closed immediately after use.

    Configuration options:
        ``compare_type=True``
            Enables column type change detection during autogenerate.
        ``compare_server_default=True``
            Enables server default change detection during autogenerate.

    The migration operations are wrapped in a transaction so that a failure
    in any individual migration step rolls back the entire batch, leaving
    the database in a consistent state.
    """
    connectable = engine_from_config(
        config.get_section(config.config_ini_section, {}),
        prefix="sqlalchemy.",
        poolclass=pool.NullPool,
    )

    with connectable.connect() as connection:
        context.configure(
            connection=connection,
            target_metadata=target_metadata,
            compare_type=True,
            compare_server_default=True,
        )

        with context.begin_transaction():
            context.run_migrations()


# ------------------------------------------------------------------------------
# Mode Dispatch
# ------------------------------------------------------------------------------
# Alembic sets the offline flag based on the ``--sql`` CLI option.
# This conditional routes to the appropriate migration runner.
# ------------------------------------------------------------------------------
if context.is_offline_mode():
    run_migrations_offline()
else:
    run_migrations_online()
