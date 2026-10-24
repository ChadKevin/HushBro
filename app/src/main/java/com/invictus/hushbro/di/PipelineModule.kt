package com.invictus.hushbro.di

import com.invictus.hushbro.data.repo.RuleRepository
import com.invictus.hushbro.pipeline.Classifier
import com.invictus.hushbro.pipeline.DefaultRedactor
import com.invictus.hushbro.pipeline.DefaultRuleEngine
import com.invictus.hushbro.pipeline.PassThroughClassifier
import com.invictus.hushbro.pipeline.Redactor
import com.invictus.hushbro.pipeline.RuleEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object PipelineModule {

    @Provides
    fun provideRedactor(): Redactor = DefaultRedactor()

    @Provides
    fun provideRuleEngine(ruleRepository: RuleRepository): RuleEngine = DefaultRuleEngine(ruleRepository)

    @Provides
    fun provideClassifier(): Classifier = PassThroughClassifier()
}
