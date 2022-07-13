package mashup.ggiriggiri.gifticonstorm.domain.sprinkle.repository

import mashup.ggiriggiri.gifticonstorm.common.dto.NoOffsetRequest
import mashup.ggiriggiri.gifticonstorm.config.QuerydslTestConfig
import mashup.ggiriggiri.gifticonstorm.domain.coupon.domain.Category
import mashup.ggiriggiri.gifticonstorm.domain.coupon.domain.Coupon
import mashup.ggiriggiri.gifticonstorm.domain.coupon.repository.CouponRepository
import mashup.ggiriggiri.gifticonstorm.domain.member.Member
import mashup.ggiriggiri.gifticonstorm.domain.member.repository.MemberRepository
import mashup.ggiriggiri.gifticonstorm.domain.participant.Participant
import mashup.ggiriggiri.gifticonstorm.domain.participant.repository.ParticipantRepository
import mashup.ggiriggiri.gifticonstorm.domain.sprinkle.domain.Sprinkle
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@DataJpaTest
@ActiveProfiles("test")
@Import(QuerydslTestConfig::class)
class SprinkleRepositoryCustomImplTest @Autowired constructor(
    private val sprinkleRepository: SprinkleRepository,
    private val couponRepository: CouponRepository,
    private val memberRepository: MemberRepository,
    private val participantRepository: ParticipantRepository
) {

    private lateinit var memberList: List<Member>
    private lateinit var couponList: List<Coupon>
    private lateinit var sprinkleList: List<Sprinkle>
    private lateinit var participantList: List<Participant>

    @BeforeEach
    fun setUp() {
        memberList = mutableListOf(
            Member(inherenceId = "testUser1"),
            Member(inherenceId = "testUser2"),
            Member(inherenceId = "testUser3")
        )
        couponList = mutableListOf(
            Coupon(
                brandName = "스타벅스",
                merchandiseName = "아이스 아메리카노",
                expiredAt = LocalDateTime.now().plusDays(1),
                imageUrl = "testUrl",
                category = Category.CAFE,
                member = memberList[0]
            ),
            Coupon(
                brandName = "BHC",
                merchandiseName = "치킨",
                expiredAt = LocalDateTime.now().plusDays(1),
                imageUrl = "testUrl",
                category = Category.DELIVERY,
                member = memberList[0]
            ),
            Coupon(
                brandName = "베스킨라빈스",
                merchandiseName = "파인트",
                expiredAt = LocalDateTime.now().plusDays(1),
                imageUrl = "testUrl",
                category = Category.ICECREAM,
                member = memberList[0]
            ),
            Coupon(
                brandName = "버거킹",
                merchandiseName = "햄버거",
                expiredAt = LocalDateTime.now().plusDays(1),
                imageUrl = "testUrl",
                category = Category.FAST_FOOD,
                member = memberList[0]
            )
        )
        sprinkleList = mutableListOf(
            Sprinkle( //마감임박 해당 데이터
                member = memberList[0],
                coupon = couponList[0],
                sprinkleAt = LocalDateTime.now().plusMinutes(10)
            ),
            Sprinkle( //마감임박 해당 X 데이터
                member = memberList[0],
                coupon = couponList[1],
                sprinkleAt = LocalDateTime.now().plusMinutes(11)
            ),
            Sprinkle( //마감임박 해당 데이터
                member = memberList[0],
                coupon = couponList[2],
                sprinkleAt = LocalDateTime.now().plusMinutes(9)
            ),
            Sprinkle( //마감임박 해당 데이터
                member = memberList[0],
                coupon = couponList[3],
                sprinkleAt = LocalDateTime.now().plusMinutes(8)
            )
        )
        participantList = mutableListOf(
            Participant(member = memberList[1], sprinkle = sprinkleList[0]),
            Participant(member = memberList[2], sprinkle = sprinkleList[0]),
            Participant(member = memberList[1], sprinkle = sprinkleList[1]),
            Participant(member = memberList[1], sprinkle = sprinkleList[2])
        )
        memberRepository.saveAll(memberList)
        couponRepository.saveAll(couponList)
        sprinkleRepository.saveAll(sprinkleList)
        participantRepository.saveAll(participantList)
    }

    @Test
    fun `findAllByDeadLine - 뿌리기 남은 시간 10분 이내 & 참여자 수 내림차순 중 상위 2개 조회`() {
        //when
        val sprinkleListVos = sprinkleRepository.findAllByDeadLine(10, 2)
        //then
        assertThat(sprinkleListVos.size).isEqualTo(2)
        assertThat(sprinkleListVos[0].sprinkleId).isEqualTo(sprinkleList[0].id)
        assertThat(sprinkleListVos[0].brandName).isEqualTo(sprinkleList[0].coupon.brandName)
        assertThat(sprinkleListVos[0].participants).isEqualTo(2)
        assertThat(sprinkleListVos[1].sprinkleId).isEqualTo(sprinkleList[2].id)
        assertThat(sprinkleListVos[1].brandName).isEqualTo(sprinkleList[2].coupon.brandName)
        assertThat(sprinkleListVos[1].participants).isEqualTo(1)
    }

    @Test
    fun `findAllByCategory - 전체 조회, NoOffset 첫 번째 페이지`() {
        //given
        val noOffsetRequest = NoOffsetRequest.of(id = null, limit = 2)
        //when
        val sprinkleListVos = sprinkleRepository.findAllByCategory(Category.ALL, noOffsetRequest)
        //then
        assertThat(sprinkleListVos).hasSize(2)
        assertThat(sprinkleListVos[0].sprinkleId).isEqualTo(sprinkleList[0].id)
        assertThat(sprinkleListVos[0].participants).isEqualTo(2)
        assertThat(sprinkleListVos[1].sprinkleId).isEqualTo(sprinkleList[1].id)
        assertThat(sprinkleListVos[1].participants).isEqualTo(1)
    }

    @Test
    fun `findAllByCategory - 전체 조회, NoOffset 두 번째 페이지`() {
        //given
        val noOffsetRequest = NoOffsetRequest.of(id = sprinkleList[1].id, limit = 2)
        //when
        val sprinkleListVos = sprinkleRepository.findAllByCategory(Category.ALL, noOffsetRequest)
        //then
        assertThat(sprinkleListVos).hasSize(2)
        assertThat(sprinkleListVos[0].sprinkleId).isEqualTo(sprinkleList[2].id)
        assertThat(sprinkleListVos[0].participants).isEqualTo(1)
        assertThat(sprinkleListVos[1].sprinkleId).isEqualTo(sprinkleList[3].id)
        assertThat(sprinkleListVos[1].participants).isEqualTo(0)
    }

    @Test
    fun `findAllByCategory - 특정 카테고리 조회`() {
        //when
        val sprinkleListVos = sprinkleRepository.findAllByCategory(Category.CAFE, NoOffsetRequest.of())
        //then
        assertThat(sprinkleListVos).hasSize(1)
        assertThat(sprinkleListVos[0].sprinkleId).isEqualTo(sprinkleList[0].id)
        assertThat(sprinkleListVos[0].participants).isEqualTo(2)
    }
}