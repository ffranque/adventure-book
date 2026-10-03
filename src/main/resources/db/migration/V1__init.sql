-- Initial schema. Hibernate runs with ddl-auto=validate, so entity mappings must match this file.

create sequence section_seq start with 1 increment by 50;
create sequence section_option_seq start with 1 increment by 50;
create sequence player_progress_seq start with 1 increment by 50;

create table book (
    id         varchar(36)  not null,
    source     varchar(255) not null,
    title      varchar(255) not null,
    author     varchar(255) not null,
    difficulty varchar(20)  not null,
    constraint pk_book primary key (id),
    constraint uk_book_source unique (source)
);

create table book_categories (
    book_id  varchar(36)  not null,
    category varchar(255) not null,
    constraint pk_book_categories primary key (book_id, category),
    constraint fk_book_categories_book foreign key (book_id) references book (id) on delete cascade
);
create index idx_book_categories_category on book_categories (category);

create table section (
    id             bigint      not null,
    book_id        varchar(36) not null,
    section_number integer     not null,
    text           varchar(2000),
    type           varchar(20) not null,
    constraint pk_section primary key (id),
    constraint uk_section_book_number unique (book_id, section_number),
    constraint fk_section_book foreign key (book_id) references book (id) on delete cascade
);

create table section_option (
    id                bigint  not null,
    section_id        bigint  not null,
    option_order      integer,
    description       varchar(255),
    goto_id           integer not null,
    type              varchar(20),
    consequence_value integer,
    text              varchar(1000),
    constraint pk_section_option primary key (id),
    constraint fk_section_option_section foreign key (section_id) references section (id) on delete cascade
);
create index idx_section_option_section on section_option (section_id);

create table player_progress (
    id                 bigint       not null,
    player_id          varchar(255) not null,
    book_id            varchar(36)  not null,
    current_section_id integer      not null,
    health             integer      not null,
    status             varchar(20)  not null,
    version            bigint       not null,
    constraint pk_player_progress primary key (id),
    constraint uk_player_progress_player_book unique (player_id, book_id),
    constraint fk_player_progress_book foreign key (book_id) references book (id)
);
